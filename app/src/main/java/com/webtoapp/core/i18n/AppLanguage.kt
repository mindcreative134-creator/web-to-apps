package com.webtoapp.core.i18n

import java.util.Locale

/**
 * Supported application languages.
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale,
    val isRtl: Boolean = false
) {
    ENGLISH("en", "English", "English", Locale.ENGLISH),
    HINDI("hi", "Hindi", "हिन्दी", Locale("hi")),
    CHINESE("zh", "Chinese", "中文", Locale.CHINESE),
    ARABIC("ar", "Arabic", "العربية", Locale("ar"), isRtl = true);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
