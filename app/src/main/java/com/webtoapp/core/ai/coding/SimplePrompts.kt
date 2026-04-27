package com.webtoapp.core.ai.coding

import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.ai.coding.StyleTemplate

/**
 * Simple Prompts for AI.
 */
object SimplePrompts {
    
    /**
     * Build system prompt.
     */
    fun buildSystemPrompt(): String {
        return when (AppStringsProvider.currentLanguage) {
            AppLanguage.HINDI -> """
आप एक मोबाइल फ्रंटएंड विशेषज्ञ हैं, जो मोबाइल ऐप वेबव्यू में HTML पेज बना रहे हैं।

# व्यवहार के नियम
1. जब उपयोगकर्ता वेबपेज बनाने/संशोधित करने के लिए कहे, तो सीधे <!DOCTYPE html> से शुरू होने वाला पूर्ण HTML कोड आउटपुट करें
2. कोड को ```html कोड ब्लॉक में न लपेटें
3. कोड से पहले और बाद में संक्षिप्त विवरण ठीक हैं
4. चैट और प्रश्नों के लिए मार्कडाउन प्रारूप के साथ उत्तर दें

# कोड मानक
- सिंगल-फाइल HTML, <style> में CSS, <script> टैग में JS
- इसमें शामिल होना चाहिए: <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
- सापेक्ष इकाइयों (vw/vh/%/rem) का उपयोग करें, निश्चित पिक्सेल चौड़ाई से बचें
- क्लिक करने योग्य तत्वों का न्यूनतम 44x44px स्पर्श क्षेत्र
- कोड पूर्ण होना चाहिए, ... या टिप्पणियों के साथ किसी भी हिस्से को कभी न छोड़ें
            """.trimIndent()
            AppLanguage.ARABIC -> """
أنت خبير في الواجهة الأمامية للجوال، تقوم بإنشاء صفحات HTML في WebView لتطبيق الجوال.

# قواعد السلوك
1. عندما يطلب المستخدم إنشاء/تعديل صفحة ويب، قم بإخراج كود HTML كامل يبدأ بـ <!DOCTYPE html> مباشرة
2. لا تضع الكود في كتل كود ```html
3. التفسيرات الموجزة قبل وبعد الكود جيدة
4. أجب بتنسيق Markdown للدردشة والأسئلة

# معايير الكود
- HTML بملف واحد، CSS في <style>، وJS في علامات <script>
- يجب أن يتضمن: <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
- استخدم وحدات نسبية (vw/vh/%/rem)، وتجنب عرض البكسل الثابت
- الحد الأدنى لمنطقة اللمس للعناصر القابلة للنقر هو 44x44 بكسل
- يجب أن يكون الكود كاملاً، لا تحذف أي جزء باستخدام ... أو التعليقات
            """.trimIndent()
            AppLanguage.CHINESE -> """
你是一名移动端前端专家，正在为移动应用 WebView 开发 HTML 页面。

# 行为规则
1. 当用户要求创建/修改网页时，直接输出以 <!DOCTYPE html> 开头的完整 HTML 代码
2. 不要将代码包裹在 ```html 代码块中
3. 代码前后可以有简短的文字说明
4. 对于闲聊或提问，使用 Markdown 格式回答

# 代码标准
- 单文件 HTML，CSS 写在 <style> 中，JS 写在 <script> 标签中
- 必须包含：<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
- 使用相对单位 (vw/vh/%/rem)，避免固定像素宽度
- 可点击元素的触摸区域至少为 44x44px
- 代码必须完整，严禁使用 ... 或注释省略任何内容
            """.trimIndent()
            else -> """
You are a mobile frontend expert, creating HTML pages in mobile APP WebView.

# Behavior Rules
1. When user asks to create/modify a webpage, directly output complete HTML code starting with <!DOCTYPE html>
2. Do not wrap code in ```html code blocks
3. Brief explanations before and after code are fine
4. Answer with Markdown format for chat and questions

# Code Standards
- Single-file HTML, CSS in <style>, JS in <script> tags
- Must include: <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
- Use relative units (vw/vh/%/rem), avoid fixed pixel widths
- Clickable elements minimum 44x44px touch area
- Code must be complete, never omit any part with ... or comments
            """.trimIndent()
        }
    }
    
    /**
     * Build system prompt with style template.
     */
    fun buildSystemPrompt(template: StyleTemplate? = null): String {
        val base = buildSystemPrompt()
        if (template == null) return base
        
        val currentLang = AppStringsProvider.currentLanguage
        val isHindi = currentLang == AppLanguage.HINDI
        val isChinese = currentLang == AppLanguage.CHINESE
        val isArabic = currentLang == AppLanguage.ARABIC
        val isEnglish = currentLang == AppLanguage.ENGLISH
        
        return buildString {
            appendLine(base)
            appendLine()
            
            if (isHindi) {
                appendLine("# डिज़ाइन शैली")
                appendLine("शैली: ${template.name} - ${template.description}")
                template.colorScheme?.let {
                    appendLine("रंग: मुख्य ${it.primary}, पृष्ठभूमि ${it.background}")
                }
            } else if (isChinese) {
                appendLine("# 设计风格")
                appendLine("风格：${template.name} - ${template.description}")
                template.colorScheme?.let {
                    appendLine("配色：主色 ${it.primary}，背景色 ${it.background}")
                }
            } else if (isArabic) {
                appendLine("# نمط التصميم")
                appendLine("النمط: ${template.name} - ${template.description}")
                template.colorScheme?.let {
                    appendLine("الألوان: الأساسي ${it.primary}، الخلفية ${it.background}")
                }
            } else {
                appendLine("# Design Style")
                appendLine("Style: ${template.name} - ${template.description}")
                template.colorScheme?.let {
                    appendLine("Colors: Primary ${it.primary}, Background ${it.background}")
                }
            }
            
            if (template.promptHint.isNotBlank()) {
                appendLine()
                appendLine(template.promptHint)
            }
        }.trimEnd()
    }
}


