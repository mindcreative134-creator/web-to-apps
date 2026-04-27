package com.webtoapp.core.frontend

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.AppStringsProvider
import java.io.File

/**
 * Sample Project Manager
 * 
 * Provides built-in sample frontend projects for user experience and testing
 */
object SampleProjectManager {
    
    private const val TAG = "SampleProjectManager"
    private const val SAMPLES_DIR = "sample_projects"
    
    /**
     * Get project ID suffix based on current language
     */
    private fun getLanguageSuffix(context: Context, baseId: String): String {
        val lang = AppStringsProvider.currentLanguage
        val suffix = when (lang) {
            AppLanguage.ENGLISH -> "-en"
            AppLanguage.HINDI -> "-hi"
            AppLanguage.CHINESE -> "-zh"
            AppLanguage.ARABIC -> "-ar"
        }
        
        // Fallback check: if localized assets don't exist, use English
        return try {
            val assets = context.assets.list("$SAMPLES_DIR/$baseId$suffix")
            if (assets.isNullOrEmpty()) "-en" else suffix
        } catch (e: Exception) {
            "-en"
        }
    }

    /**
     * Get all sample projects (dynamically selected based on current language)
     */
    fun getSampleProjects(context: Context): List<SampleProject> {
        return listOf(
            SampleProject(
                id = "vue-demo${getLanguageSuffix(context, "vue-demo")}",
                name = AppStringsProvider.current().sampleVueCounterName,
                description = AppStringsProvider.current().sampleVueCounterDesc,
                framework = FrontendFramework.VUE,
                icon = "🟢",
                tags = listOf("Vue 3", AppStringsProvider.current().sampleVueCounterTagReactive)
            ),
            SampleProject(
                id = "react-demo${getLanguageSuffix(context, "react-demo")}",
                name = AppStringsProvider.current().sampleReactTodoName,
                description = AppStringsProvider.current().sampleReactTodoDesc,
                framework = FrontendFramework.REACT,
                icon = "⚛️",
                tags = listOf("React 18", "Hooks")
            ),
            SampleProject(
                id = "vite-vanilla${getLanguageSuffix(context, "vite-vanilla")}",
                name = AppStringsProvider.current().sampleWeatherAppName,
                description = AppStringsProvider.current().sampleWeatherAppDesc,
                framework = FrontendFramework.VITE,
                icon = "🌤️",
                tags = listOf("Vite", "Vanilla JS")
            )
        )
    }
    
    /**
     * Extract sample projects to app directory
     * 
     * @return Extracted project path
     */
    suspend fun extractSampleProject(
        context: Context,
        projectId: String,
        forceRefresh: Boolean = false
    ): Result<String> {
        return try {
            val outputDir = File(context.filesDir, "sample_projects/$projectId")
            
            // Check version marker file to ensure re-extraction if assets update
            val versionFile = File(outputDir, ".version")
            val currentVersion = getAppVersionCode(context)
            val cachedVersion = if (versionFile.exists()) versionFile.readText().trim().toLongOrNull() else null
            
            // If project already exists and version matches and no force refresh, return existing path
            if (outputDir.exists() && cachedVersion == currentVersion && !forceRefresh) {
                AppLogger.d(TAG, "Sample project exists and version matches: ${outputDir.absolutePath}")
                return Result.success(outputDir.absolutePath)
            }
            
            AppLogger.i(TAG, "Re-extracting sample project (Version: $cachedVersion -> $currentVersion)")
            
            // Cleanup and create directory
            outputDir.deleteRecursively()
            outputDir.mkdirs()
            
            // Copy files from assets
            val assetPath = "$SAMPLES_DIR/$projectId"
            copyAssetFolder(context, assetPath, outputDir)
            
            // Write version marker
            versionFile.writeText(currentVersion.toString())
            
            AppLogger.i(TAG, "Sample project extracted: ${outputDir.absolutePath}")
            Result.success(outputDir.absolutePath)
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract sample project", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get app版本号
     */
    private fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get app version code
     */
    suspend fun getSampleDistPath(
        context: Context,
        projectId: String
    ): Result<String> {
        val extractResult = extractSampleProject(context, projectId)
        return extractResult.map { projectPath ->
            "$projectPath/dist"
        }
    }
    
    /**
     * Copy assets folder
     */
    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        val assetManager = context.assets
        
        try {
            val files = assetManager.list(assetPath) ?: return
            
            if (files.isEmpty()) {
                // It's a file, copy directly
                assetManager.open(assetPath).use { input ->
                    File(targetDir.parent, targetDir.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // It's a directory, copy recursively
                targetDir.mkdirs()
                for (file in files) {
                    copyAssetFolder(context, "$assetPath/$file", File(targetDir, file))
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to copy file: $assetPath", e)
        }
    }
    
    /**
     * Clear all extracted sample projects
     */
    fun clearExtractedProjects(context: Context) {
        val samplesDir = File(context.filesDir, "sample_projects")
        if (samplesDir.exists()) {
            samplesDir.deleteRecursively()
        }
    }
}

/**
 * Sample Project Information
 */
data class SampleProject(
    val id: String,
    val name: String,
    val description: String,
    val framework: FrontendFramework,
    val icon: String,
    val tags: List<String>
)


