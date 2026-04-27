package com.webtoapp.core.extension.agent

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.google.gson.JsonParser
import com.webtoapp.core.ai.AiApiClient
import com.webtoapp.core.ai.AiConfigManager
import com.webtoapp.core.ai.StreamEvent
import com.webtoapp.core.extension.*
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.i18n.AiPromptManager
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.LanguageManager
import com.webtoapp.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CancellationException

/**
 * Enhanced Agent Engine
 * 
 * Core Agent implementation, supporting:
 * - Streaming output (developWithStream)
 * - ReAct loop
 * - Toolchain calling
 * - Auto-fix (up to 3 times, using iteration instead of recursion)
 * - Context maintenance
 * - Timeout protection
 * 
 * Requirements: 2.1, 2.6, 5.2
 */
class EnhancedAgentEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "EnhancedAgentEngine"
        private const val STREAM_TIMEOUT_MS = 120_000L  // 2 minutes timeout
        private const val MAX_FIX_ATTEMPTS = 3
        
        // Code block parsing
        private val JSON_BLOCK_REGEX = Regex("```json\\s*([\\s\\S]*?)\\s*```")
        private val JS_BLOCK_REGEX = Regex("```(?:javascript|js)\\s*([\\s\\S]*?)\\s*```")
        private val CSS_BLOCK_REGEX = Regex("```css\\s*([\\s\\S]*?)\\s*```")
    }
    
    private val gson = com.webtoapp.util.GsonProvider.gson
    private val aiConfigManager = AiConfigManager(context)
    private val aiClient = AiApiClient(context)
    private val toolExecutor = AgentToolExecutor(context)
    private val languageManager by lazy { LanguageManager(context.applicationContext) }
    
    // 工作记忆
    val workingMemory = AgentWorkingMemory()
    
    // 当前状态
    private val _currentState = MutableStateFlow(AgentState.IDLE)
    val currentState: StateFlow<AgentState> = _currentState.asStateFlow()
    
    /**
     * Use streaming output for module development
     * 
     * @param requirement User requirement description
     * @param model Specified model (optional)
     * @param category Module category (optional)
     * @param existingCode Existing code (for modification)
     * @return Flow<AgentStreamEvent> Streaming event flow
     */
    fun developWithStream(
        requirement: String,
        model: SavedModel? = null,
        category: ModuleCategory? = null,
        existingCode: String? = null
    ): Flow<AgentStreamEvent> = flow {
        // Initialize working memory
        workingMemory.currentRequirement = requirement
        workingMemory.addUserMessage(requirement)
        workingMemory.resetFixAttempts()  // Reset修复计数
        
        try {
            // Get AI 配置
            val apiKeys = aiConfigManager.apiKeysFlow.first()
            val savedModels = aiConfigManager.savedModelsFlow.first()
            
            if (apiKeys.isEmpty()) {
                emit(AgentStreamEvent.Error(AppStringsProvider.current().aiErrorNoApiKey, code = "NO_API_KEY"))
                return@flow
            }
            
            // Select模型
            val selectedModel = selectModel(model, savedModels)
            if (selectedModel == null) {
                emit(AgentStreamEvent.Error(AppStringsProvider.current().aiErrorNoModel, code = "NO_MODEL"))
                return@flow
            }
            
            val apiKey = apiKeys.find { it.id == selectedModel.apiKeyId }
            if (apiKey == null) {
                emit(AgentStreamEvent.Error(AppStringsProvider.current().aiErrorNoApiKeyForModel, code = "NO_API_KEY_FOR_MODEL"))
                return@flow
            }
            
            // Start development process
            emit(AgentStreamEvent.StateChange(AgentState.THINKING))
            _currentState.value = AgentState.THINKING
            
            // Build system prompt and messages
            val systemPrompt = buildSystemPrompt(category, existingCode)
            val messages = buildMessages(systemPrompt, requirement, category, existingCode)
            
            // Use streaming API call (with timeout protection)
            emit(AgentStreamEvent.StateChange(AgentState.GENERATING))
            _currentState.value = AgentState.GENERATING
            
            val contentBuilder = StringBuilder()
            val thinkingBuilder = StringBuilder()
            var streamCompleted = false
            
            try {
                withTimeout(STREAM_TIMEOUT_MS) {
                    aiClient.chatStream(apiKey, selectedModel.model, messages)
                        .collect { event ->
                            when (event) {
                                is StreamEvent.Started -> {
                                    AppLogger.w(TAG, "Stream started")
                                }
                                is StreamEvent.Thinking -> {
                                    thinkingBuilder.append(event.content)
                                    emit(AgentStreamEvent.Thinking(event.content, thinkingBuilder.toString()))
                                }
                                is StreamEvent.Content -> {
                                    contentBuilder.clear()
                                    contentBuilder.append(event.accumulated)
                                    emit(AgentStreamEvent.Content(event.delta, event.accumulated))
                                }
                                is StreamEvent.Done -> {
                                    streamCompleted = true
                                    AppLogger.w(TAG, "Stream done, content length: ${event.fullContent.length}")
                                }
                                is StreamEvent.Error -> {
                                    throw Exception(event.message)
                                }
                            }
                        }
                }
            } catch (e: TimeoutCancellationException) {
                AppLogger.e(TAG, "Stream timeout after ${STREAM_TIMEOUT_MS}ms")
                emit(AgentStreamEvent.Error(
                    message = AppStringsProvider.current().agentRequestTimeout,
                    code = "TIMEOUT",
                    recoverable = true,
                    rawResponse = contentBuilder.toString().takeIf { it.isNotEmpty() }
                ))
                return@flow
            }
            
            if (!streamCompleted || contentBuilder.isEmpty()) {
                emit(AgentStreamEvent.Error(
                    message = "AI response is empty, please try again",
                    code = "EMPTY_RESPONSE",
                    recoverable = true
                ))
                return@flow
            }
            
            // Stream complete, process generated content
            val responseText = contentBuilder.toString()
            processGeneratedContentIterative(responseText, apiKey, selectedModel)
                .collect { agentEvent -> emit(agentEvent) }
                
        } catch (e: CancellationException) {
            throw e  // Re-throw cancellation exception
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error in developWithStream", e)
            emit(AgentStreamEvent.StateChange(AgentState.ERROR))
            _currentState.value = AgentState.ERROR
            workingMemory.lastError = e.message
            emit(AgentStreamEvent.Error(
                message = e.message ?: AppStringsProvider.current().aiErrorUnknown,
                recoverable = true
            ))
        }
    }.flowOn(Dispatchers.IO)

    
    /**
     * Process generated content (iterative version to avoid recursive Flow issues)
     * Parse modules, execute syntax check, auto-fix
     */
    private fun processGeneratedContentIterative(
        responseText: String,
        apiKey: ApiKeyConfig,
        savedModel: SavedModel
    ): Flow<AgentStreamEvent> = flow {
        // Parse generated module
        val parsedModule = parseGeneratedModule(responseText)
        if (parsedModule == null) {
            emit(AgentStreamEvent.Error(
                message = AppStringsProvider.current().agentParseFailed,
                rawResponse = responseText
            ))
            return@flow
        }
        
        var currentModule: GeneratedModuleData = parsedModule
        workingMemory.updateModule(currentModule)
        emit(AgentStreamEvent.ModuleGenerated(currentModule))
        
        // Syntax check and auto-fix loop (iterative instead of recursive)
        var fixAttempt = 0
        var syntaxValid = false
        
        while (fixAttempt <= MAX_FIX_ATTEMPTS && !syntaxValid) {
            // Execute syntax check
            emit(AgentStreamEvent.StateChange(AgentState.SYNTAX_CHECKING))
            _currentState.value = AgentState.SYNTAX_CHECKING
            
            val syntaxCheckRequest = ToolCallRequest(
                toolName = "syntax_check",
                arguments = mapOf("code" to currentModule.jsCode, "language" to "javascript")
            )
            
            val syntaxToolInfo = ToolCallInfo.fromRequest(syntaxCheckRequest)
                .copy(status = ToolStatus.EXECUTING)
            emit(AgentStreamEvent.ToolStart(syntaxToolInfo))
            workingMemory.recordToolCall(syntaxToolInfo)
            
            val syntaxResult = toolExecutor.execute(syntaxCheckRequest)
            val completedSyntaxInfo = ToolCallInfo.fromResult(syntaxToolInfo, syntaxResult)
            emit(AgentStreamEvent.ToolComplete(completedSyntaxInfo))
            workingMemory.updateToolCallResult(syntaxToolInfo.callId, syntaxResult)
            
            val syntaxCheck = syntaxResult.result as? SyntaxCheckResult
            
            if (syntaxCheck == null || syntaxCheck.valid) {
                syntaxValid = true
                AppLogger.w(TAG, "Syntax check passed")
            } else {
                // Syntax error, attempt fix
                fixAttempt++
                
                if (fixAttempt > MAX_FIX_ATTEMPTS) {
                    // Maximum fix attempts reached
                    val errorMessage = buildAutoFixLimitErrorMessage(syntaxCheck)
                    emit(AgentStreamEvent.Error(
                        message = errorMessage,
                        code = "MAX_FIX_ATTEMPTS_REACHED",
                        recoverable = true
                    ))
                    break
                }
                
                AppLogger.w(TAG, "Syntax errors found, attempting fix $fixAttempt/$MAX_FIX_ATTEMPTS")
                
                // Attempt fix
                emit(AgentStreamEvent.StateChange(AgentState.FIXING))
                _currentState.value = AgentState.FIXING
                
                val fixedModule = tryFixSyntaxErrors(currentModule, syntaxCheck, apiKey, savedModel, fixAttempt)
                
                if (fixedModule != null) {
                    currentModule = fixedModule
                    workingMemory.updateModule(currentModule)
                    emit(AgentStreamEvent.ModuleGenerated(currentModule))
                } else {
                    // Fix failed
                    emit(AgentStreamEvent.Error(
                        message = AppStringsProvider.current().agentAutoFixFailed,
                        code = "AUTO_FIX_FAILED",
                        recoverable = true
                    ))
                    break
                }
            }
        }
        
        // Execute security scan
        emit(AgentStreamEvent.StateChange(AgentState.SECURITY_SCANNING))
        _currentState.value = AgentState.SECURITY_SCANNING
        
        val securityRequest = ToolCallRequest(
            toolName = "security_scan",
            arguments = mapOf("code" to currentModule.jsCode)
        )
        
        val securityToolInfo = ToolCallInfo.fromRequest(securityRequest)
            .copy(status = ToolStatus.EXECUTING)
        emit(AgentStreamEvent.ToolStart(securityToolInfo))
        workingMemory.recordToolCall(securityToolInfo)
        
        val securityResult = toolExecutor.execute(securityRequest)
        val completedSecurityInfo = ToolCallInfo.fromResult(securityToolInfo, securityResult)
        emit(AgentStreamEvent.ToolComplete(completedSecurityInfo))
        workingMemory.updateToolCallResult(securityToolInfo.callId, securityResult)
        
        val securityScan = securityResult.result as? SecurityScanResult
        val finalModule = currentModule.copy(
            securitySafe = securityScan?.safe ?: true
        )
        workingMemory.updateModule(finalModule)
        
        // Done
        emit(AgentStreamEvent.StateChange(AgentState.COMPLETED))
        _currentState.value = AgentState.COMPLETED
        
        // Save to conversation history
        workingMemory.addAssistantMessage(
            content = AppStringsProvider.current().agentModuleGenerated.replace("%s", finalModule.name),
            generatedModule = finalModule
        )
        
        emit(AgentStreamEvent.Completed(finalModule))
    }
    
    /**
     * Attempt to fix syntax errors (single fix, non-recursive, localized support)
     * 
     * @return Fixed module, or null if fix failed
     */
    private suspend fun tryFixSyntaxErrors(
        module: GeneratedModuleData,
        syntaxResult: SyntaxCheckResult,
        apiKey: ApiKeyConfig,
        savedModel: SavedModel,
        attemptNumber: Int
    ): GeneratedModuleData? {
        // Get当前语言
        val currentLanguage = languageManager.getCurrentLanguage()
        
        val errorMessages = syntaxResult.errors.joinToString("\n") { error ->
            "- Line ${error.line}, Column ${error.column}: ${error.message}" +
                (error.suggestion?.let { "\n  Suggestion: $it" } ?: "")
        }
        
        val fixPrompt = AiPromptManager.getCodeFixPrompt(
            language = currentLanguage,
            errorMessages = errorMessages,
            code = module.jsCode,
            attempt = attemptNumber,
            maxAttempts = MAX_FIX_ATTEMPTS
        )
        
        val systemPrompt = AiPromptManager.getCodeFixSystemPrompt(currentLanguage)
        
        val messages = listOf(
            mapOf("role" to "system", "content" to systemPrompt),
            mapOf("role" to "user", "content" to fixPrompt)
        )
        
        return try {
            val response = withTimeout(60_000) {
                aiClient.chat(apiKey, savedModel.model, messages)
            }
            
            if (response.isSuccess) {
                val fixedCode = response.getOrNull() ?: return null
                
                // Extract code blocks
                val code = JS_BLOCK_REGEX.find(fixedCode)?.groupValues?.get(1) ?: fixedCode
                
                if (code.isBlank()) return null
                
                module.copy(jsCode = code.trim())
            } else {
                AppLogger.e(TAG, "Fix request failed: ${response.exceptionOrNull()?.message}")
                null
            }
        } catch (e: TimeoutCancellationException) {
            AppLogger.e(TAG, "Fix request timeout")
            null
        } catch (e: Exception) {
            AppLogger.e(TAG, "Fix request error", e)
            null
        }
    }
    
    /**
     * Build error message for auto-fix limit reached
     */
    private fun buildAutoFixLimitErrorMessage(syntaxResult: SyntaxCheckResult): String {
        val errorSummary = syntaxResult.errors.take(3).joinToString("\n") { error ->
            "  - Line ${error.line}: ${error.message}"
        }
        val moreErrors = if (syntaxResult.errors.size > 3) {
            "\n  ... and ${syntaxResult.errors.size - 3} more errors"
        } else ""
        
        return """
Maximum auto-fix attempts reached (${MAX_FIX_ATTEMPTS}), code still has syntax errors, please fix manually:
$errorSummary$moreErrors
        """.trimIndent()
    }
    
    /**
     * 使用工具链执行语法检查和自动修复
     * 
     * 这是一个更高级的自动修复方法，使用 AgentToolExecutor 的工具链功能
     * 
     * Requirements: 5.3, 5.4, 5.5
     */
    fun performSyntaxCheckAndAutoFix(
        code: String,
        language: String = "javascript"
    ): Flow<AgentStreamEvent> = flow {
        emit(AgentStreamEvent.StateChange(AgentState.SYNTAX_CHECKING))
        _currentState.value = AgentState.SYNTAX_CHECKING
        
        toolExecutor.executeSyntaxCheckAndFixChain(
            code = code,
            language = language,
            maxFixAttempts = workingMemory.maxFixAttempts
        ).collect { chainEvent ->
            when (chainEvent) {
                is ToolChainEvent.ChainStarted -> {
                    // 链开始，不需要特殊处理
                }
                is ToolChainEvent.ToolStarted -> {
                    val toolInfo = ToolCallInfo.fromRequest(chainEvent.request)
                        .copy(status = ToolStatus.EXECUTING)
                    emit(AgentStreamEvent.ToolStart(toolInfo))
                    workingMemory.recordToolCall(toolInfo)
                }
                is ToolChainEvent.ToolCompleted -> {
                    val toolInfo = ToolCallInfo(
                        toolName = chainEvent.result.toolName,
                        callId = chainEvent.result.callId,
                        status = if (chainEvent.result.success) ToolStatus.SUCCESS else ToolStatus.FAILED,
                        result = chainEvent.result.result,
                        error = chainEvent.result.error,
                        executionTimeMs = chainEvent.result.executionTimeMs
                    )
                    emit(AgentStreamEvent.ToolComplete(toolInfo))
                }
                is ToolChainEvent.ChainCompleted -> {
                    emit(AgentStreamEvent.StateChange(AgentState.COMPLETED))
                    _currentState.value = AgentState.COMPLETED
                }
                is ToolChainEvent.ChainFailed -> {
                    emit(AgentStreamEvent.StateChange(AgentState.ERROR))
                    _currentState.value = AgentState.ERROR
                    emit(AgentStreamEvent.Error(
                        message = chainEvent.error,
                        code = "TOOL_CHAIN_FAILED",
                        recoverable = true
                    ))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 选择模型
     */
    private suspend fun selectModel(
        preferredModel: SavedModel?,
        savedModels: List<SavedModel>
    ): SavedModel? {
        // 如果指定了模型，直接使用
        if (preferredModel != null) {
            return preferredModel
        }
        
        // 优先使用支持模块开发功能的模型
        val moduleDevModels = savedModels.filter { it.supportsFeature(AiFeature.MODULE_DEVELOPMENT) }
        val defaultModelId = aiConfigManager.defaultModelIdFlow.first()
        
        return moduleDevModels.find { it.id == defaultModelId }
            ?: moduleDevModels.firstOrNull()
            ?: savedModels.find { it.id == defaultModelId }
            ?: savedModels.firstOrNull()
    }

    
    /**
     * Build system prompt (supports English/Hindi)
     */
    private suspend fun buildSystemPrompt(category: ModuleCategory?, existingCode: String?): String {
        // Get current language
        val currentLanguage = languageManager.getCurrentLanguage()
        
        val categoryHint: String = category?.let {
            when (currentLanguage) {
                AppLanguage.HINDI -> """
## लक्षित श्रेणी
उपयोगकर्ता "${it.getDisplayName()}" प्रकार का मॉड्यूल बनाना चाहता है।
श्रेणी विवरण: ${it.getDescription()}
                """.trimIndent()
                else -> """
## Target Category
User wants to create a "${it.getDisplayName()}" type module.
Category description: ${it.getDescription()}
                """.trimIndent()
            }
        } ?: ""
        
        val existingCodeHint: String = existingCode?.let {
            when (currentLanguage) {
                AppLanguage.HINDI -> """
## मौजूदा कोड
उपयोगकर्ता ने मौजूदा कोड प्रदान किया है, कृपया इसके आधार पर संशोधित या अनुकूलित करें:
```javascript
$it
```
                """.trimIndent()
                else -> """
## Existing Code
User provided existing code, please modify or optimize based on this:
```javascript
$it
```
                """.trimIndent()
            }
        } ?: ""
        
        // Get NativeBridge API 文档
        val nativeBridgeApi = com.webtoapp.core.webview.NativeBridge.getApiDocumentation()
        
        return AiPromptManager.getModuleDevelopmentSystemPrompt(
            language = currentLanguage,
            categoryHint = categoryHint,
            existingCodeHint = existingCodeHint,
            nativeBridgeApi = nativeBridgeApi
        )
    }

    /**
     * 构建消息列表（支持多语言）
     */
    private suspend fun buildMessages(
        systemPrompt: String,
        requirement: String,
        category: ModuleCategory?,
        existingCode: String?
    ): List<Map<String, String>> {
        val messages = mutableListOf<Map<String, String>>()
        
        // Get当前语言
        val currentLanguage = languageManager.getCurrentLanguage()
        
        // System消息
        messages.add(mapOf("role" to "system", "content" to systemPrompt))
        
        // 添加对话历史（保持上下文）
        workingMemory.getContextForAi()
            .filter { it["role"] != "system" }
            .forEach { messages.add(it) }
        
        // 如果对话历史为空，添加用户消息
        if (workingMemory.conversationHistory.isEmpty()) {
            val userMessage = AiPromptManager.getUserMessageTemplate(
                language = currentLanguage,
                requirement = requirement,
                categoryName = category?.getDisplayName(),
                existingCode = existingCode
            )
            
            messages.add(mapOf("role" to "user", "content" to userMessage))
        }
        
        return messages
    }
    
    /**
     * Parse generated module
     */
    private fun parseGeneratedModule(response: String): GeneratedModuleData? {
        return try {
            // Attempt to extract JSON block
            val jsonMatch = JSON_BLOCK_REGEX.find(response)
            
            val jsonStr = if (jsonMatch != null) {
                jsonMatch.groupValues[1]
            } else {
                // Attempt direct parse
                response.trim()
            }
            
            val json = JsonParser.parseString(jsonStr).asJsonObject
            
            GeneratedModuleData(
                name = json.get("name")?.asString ?: "AI Generated Module",
                description = json.get("description")?.asString ?: "",
                icon = json.get("icon")?.asString ?: "🤖",
                category = json.get("category")?.asString ?: "OTHER",
                jsCode = json.get("js_code")?.asString ?: json.get("jsCode")?.asString ?: "",
                cssCode = json.get("css_code")?.asString ?: json.get("cssCode")?.asString ?: "",
                configItems = parseConfigItems(json),
                urlMatches = json.getAsJsonArray("url_matches")?.map { it.asString } ?: emptyList(),
                runAt = json.get("run_at")?.asString ?: json.get("runAt")?.asString ?: "DOCUMENT_END"
            )
        } catch (e: Exception) {
            // 尝试从纯代码响应中提取
            extractCodeFromResponse(response)
        }
    }
    
    /**
     * 解析配置项
     */
    private fun parseConfigItems(json: com.google.gson.JsonObject): List<Map<String, Any>> {
        val items = json.getAsJsonArray("config_items") ?: json.getAsJsonArray("configItems")
        return items?.mapNotNull { item ->
            try {
                val obj = item.asJsonObject
                mapOf(
                    "key" to (obj.get("key")?.asString ?: ""),
                    "name" to (obj.get("name")?.asString ?: ""),
                    "description" to (obj.get("description")?.asString ?: ""),
                    "type" to (obj.get("type")?.asString ?: "TEXT"),
                    "defaultValue" to (obj.get("defaultValue")?.asString ?: obj.get("default_value")?.asString ?: ""),
                    "options" to (obj.getAsJsonArray("options")?.map { it.asString } ?: emptyList<String>())
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
    
    /**
     * 从纯代码响应中提取
     */
    private fun extractCodeFromResponse(response: String): GeneratedModuleData? {
        val jsCode = JS_BLOCK_REGEX.find(response)?.groupValues?.get(1)
        val cssCode = CSS_BLOCK_REGEX.find(response)?.groupValues?.get(1) ?: ""
        
        if (jsCode.isNullOrBlank()) {
            return null
        }
        
        return GeneratedModuleData(
            name = AppStringsProvider.current().aiGeneratedModule,
            description = AppStringsProvider.current().aiGeneratedModuleDesc,
            icon = "smart_toy",
            category = "OTHER",
            jsCode = jsCode,
            cssCode = cssCode
        )
    }
    
    /**
     * 执行工具链
     * 按顺序执行多个工具调用
     */
    fun executeToolChain(
        tools: List<ToolCallRequest>
    ): Flow<AgentStreamEvent> = flow {
        for (request in tools) {
            val toolInfo = ToolCallInfo.fromRequest(request)
                .copy(status = ToolStatus.EXECUTING)
            emit(AgentStreamEvent.ToolStart(toolInfo))
            workingMemory.recordToolCall(toolInfo)
            
            val result = toolExecutor.execute(request)
            val completedInfo = ToolCallInfo.fromResult(toolInfo, result)
            emit(AgentStreamEvent.ToolComplete(completedInfo))
            workingMemory.updateToolCallResult(toolInfo.callId, result)
            
            // If tool execution fails, stop chain execution
            if (!result.success) {
                emit(AgentStreamEvent.Error(
                    message = String.format(AppStringsProvider.current().agentToolFailed, request.toolName, result.error),
                    recoverable = true
                ))
                break
            }
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Reset engine state
     */
    fun reset() {
        workingMemory.reset()
        _currentState.value = AgentState.IDLE
    }
}





