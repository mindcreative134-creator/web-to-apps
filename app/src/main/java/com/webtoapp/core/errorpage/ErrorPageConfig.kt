package com.webtoapp.core.errorpage

/**
 * network error.
 */
enum class ErrorPageMode {
    /** default error system. */
    DEFAULT,
    /** usage. */
    BUILTIN_STYLE,
    /** load user. */
    CUSTOM_HTML,
    /** Note. */
    CUSTOM_MEDIA
}

/**
 * error.
 */
enum class ErrorPageStyle(val displayName: String) {
    MATERIAL("Material Design"),
    SATELLITE("Space Satellite"),
    OCEAN("Deep Sea"),
    FOREST("Firefly Forest"),
    MINIMAL("Minimalist"),
    NEON("Cyber Neon")
}

/**
 * Note.
 */
enum class MiniGameType(val displayName: String) {
    RANDOM("Random"),
    BREAKOUT("Brick Breaker"),
    MAZE("Maze Walker"),
    INK_ZEN("Ink Zen"),
    STAR_CATCH("Star Catch")
}

/**
 * config network error.
 */
data class ErrorPageConfig(
    /** Note. */
    val mode: ErrorPageMode = ErrorPageMode.BUILTIN_STYLE,
    /** BUILTIN_STYLE. */
    val builtInStyle: ErrorPageStyle = ErrorPageStyle.MATERIAL,
    /** user. */
    val customHtml: String? = null,
    /** path media. */
    val customMediaPath: String? = null,
    /** entry. */
    val showMiniGame: Boolean = false,
    /** Note. */
    val miniGameType: MiniGameType = MiniGameType.RANDOM,
    /** usage. */
    val retryButtonText: String = "",
    /** auto. */
    val autoRetrySeconds: Int = 15,
    /** locale. */
    val language: String = "ENGLISH"
)
