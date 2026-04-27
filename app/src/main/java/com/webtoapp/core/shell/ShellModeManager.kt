package com.webtoapp.core.shell

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.webtoapp.core.crypto.AssetDecryptor
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.core.logging.AppLogger

/** Escape a string for use inside a JS single-quoted string literal. */
private fun String.escapeForJsSingleQuote(): String =
    this.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\u2028", "\\u2028")
        .replace("\u2029", "\\u2029")

/** Escape a string for use inside a JS template literal (backtick). */
private fun String.escapeForJsTemplate(): String =
    this.replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("\${", "\\\${")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

/**
 * Shell Mode Manager
 * Detects if the app is running in Shell mode (standalone WebApp)
 * Supports encrypted and non-encrypted configuration files
 */
class ShellModeManager(private val context: Context) {

    companion object {
        private const val TAG = "ShellModeManager"
        private const val CONFIG_FILE = "app_config.json"
    }

    private val gson = com.webtoapp.util.GsonProvider.gson
    @Volatile
    private var cachedConfig: ShellConfig? = null
    @Volatile
    private var configLoaded = false
    private val assetDecryptor = AssetDecryptor(context)

    /**
     * Check if in Shell mode (valid configuration file exists)
     */
    fun isShellMode(): Boolean {
        return loadConfig() != null
    }

    /**
     * Get Shell configuration
     */
    fun getConfig(): ShellConfig? {
        return loadConfig()
    }

    /**
     * Load configuration file (supports encrypted and non-encrypted)
     */
    private fun loadConfig(): ShellConfig? {
        if (configLoaded) return cachedConfig

        synchronized(this) {
            if (configLoaded) return cachedConfig

            configLoaded = true
            cachedConfig = try {
            AppLogger.d(TAG, "Attempting to load configuration file: $CONFIG_FILE")
            
            // Use AssetDecryptor to automatically handle encrypted/decrypted config
            val jsonStr = try {
                assetDecryptor.loadAssetAsString(CONFIG_FILE)
            } catch (e: Exception) {
                AppLogger.e(TAG, "AssetDecryptor load failed, attempting direct read", e)
                // Fallback: read directly from assets (non-encrypted mode)
                try {
                    context.assets.open(CONFIG_FILE).bufferedReader().use { it.readText() }
                } catch (e2: Exception) {
                    AppLogger.e(TAG, "Direct read also failed", e2)
                    throw e2
                }
            }
            
            AppLogger.d(TAG, "Configuration file content length: ${jsonStr.length}")
            val config = gson.fromJson(jsonStr, ShellConfig::class.java)
            val normalizedAppType = config?.appType?.trim()?.uppercase() ?: ""
            AppLogger.d(TAG, "Parsing result: appType=${config?.appType} (normalized=$normalizedAppType)")
            val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                // Output detailed configuration info in debug mode; won't leak in release builds
                AppLogger.d(TAG, "WebView UA config: userAgentMode=${config?.webViewConfig?.userAgentMode}")
                AppLogger.d(TAG, "Injected scripts: ${config?.webViewConfig?.injectScripts?.size ?: 0}")
                AppLogger.d(TAG, "Extension modules: extensionModuleIds=${config?.extensionModuleIds?.size ?: 0}, embeddedExtensionModules=${config?.embeddedExtensionModules?.size ?: 0}")
            }
            // Verify configuration validity
            // HTML/FRONTEND apps don't need targetUrl, they use embedded HTML files
            // Media apps don't need targetUrl, they use embedded media files
            // Gallery apps don't need targetUrl, they use embedded image/video lists
            val (isValid, reason) = when {
                normalizedAppType == "HTML" || normalizedAppType == "FRONTEND" -> {
                    val entryFile = config?.htmlConfig?.entryFile ?: ""
                    if (entryFile.isBlank()) {
                        false to "Entry file is blank for $normalizedAppType"
                    } else if (entryFile.substringBeforeLast(".").isBlank()) {
                        false to "Invalid entry file format: $entryFile"
                    } else {
                        true to ""
                    }
                }
                normalizedAppType == "IMAGE" || normalizedAppType == "VIDEO" -> true to ""
                normalizedAppType == "GALLERY" -> true to ""
                normalizedAppType == "WORDPRESS" -> true to ""
                normalizedAppType == "NODEJS_APP" -> true to ""
                normalizedAppType == "PHP_APP" -> true to ""
                normalizedAppType == "PYTHON_APP" -> true to ""
                normalizedAppType == "GO_APP" -> true to ""
                else -> {
                    if (config?.targetUrl.isNullOrBlank()) {
                        false to "Target URL is missing for WEB app"
                    } else {
                        true to ""
                    }
                }
            }
            if (!isValid) {
                AppLogger.w(TAG, "Invalid configuration: $reason (appType=${config?.appType}, targetUrl=${config?.targetUrl})")
                null
            } else {
                AppLogger.d(TAG, "Configuration valid, entering Shell mode, appType=${config?.appType}")
                config
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load configuration file", e)
            null
        } catch (e: Error) {
            // Catch all Errors (including NoClassDefFoundError etc.)
            AppLogger.e(TAG, "Serious error occurred while loading configuration", e)
            null
        }

            return cachedConfig
        }
    }
    
    /**
     * Reload configuration
     */
    fun reload() {
        synchronized(this) {
            configLoaded = false
            cachedConfig = null
            assetDecryptor.clearCache()
        }
    }
}

/**
 * Shell Mode Configuration Data Class
 */
data class ShellConfig(
    @SerializedName("appName")
    val appName: String = "",

    @SerializedName("packageName")
    val packageName: String = "",

    @SerializedName("targetUrl")
    val targetUrl: String = "",

    @SerializedName("versionCode")
    val versionCode: Int = 1,

    @SerializedName("versionName")
    val versionName: String = "1.0.0",

    // Activation code config
    @SerializedName("activationEnabled")
    val activationEnabled: Boolean = false,

    @SerializedName("activationCodes")
    val activationCodes: List<String> = emptyList(),
    
    @SerializedName("activationRequireEveryTime")
    val activationRequireEveryTime: Boolean = false,

    @SerializedName("activationDialogTitle")
    val activationDialogTitle: String = "",

    @SerializedName("activationDialogSubtitle")
    val activationDialogSubtitle: String = "",

    @SerializedName("activationDialogInputLabel")
    val activationDialogInputLabel: String = "",

    @SerializedName("activationDialogButtonText")
    val activationDialogButtonText: String = "",

    // Ad blocking config
    @SerializedName("adBlockEnabled")
    val adBlockEnabled: Boolean = false,

    @SerializedName("adBlockRules")
    val adBlockRules: List<String> = emptyList(),

    // Announcement config
    @SerializedName("announcementEnabled")
    val announcementEnabled: Boolean = false,

    @SerializedName("announcementTitle")
    val announcementTitle: String = "",

    @SerializedName("announcementContent")
    val announcementContent: String = "",

    @SerializedName("announcementLink")
    val announcementLink: String = "",
    
    @SerializedName("announcementLinkText")
    val announcementLinkText: String = "",
    
    @SerializedName("announcementTemplate")
    val announcementTemplate: String = "XIAOHONGSHU",
    
    @SerializedName("announcementShowEmoji")
    val announcementShowEmoji: Boolean = true,
    
    @SerializedName("announcementAnimationEnabled")
    val announcementAnimationEnabled: Boolean = true,
    
    @SerializedName("announcementShowOnce")
    val announcementShowOnce: Boolean = true,
    
    @SerializedName("announcementRequireConfirmation")
    val announcementRequireConfirmation: Boolean = false,
    
    @SerializedName("announcementAllowNeverShow")
    val announcementAllowNeverShow: Boolean = false,

    // WebView 配置
    @SerializedName("webViewConfig")
    val webViewConfig: WebViewShellConfig = WebViewShellConfig(),

    // Splash screen config
    @SerializedName("splashEnabled")
    val splashEnabled: Boolean = false,

    @SerializedName("splashType")
    val splashType: String = "IMAGE",

    @SerializedName("splashDuration")
    val splashDuration: Int = 3,

    @SerializedName("splashClickToSkip")
    val splashClickToSkip: Boolean = true,

    // Video crop config
    @SerializedName("splashVideoStartMs")
    val splashVideoStartMs: Long = 0,

    @SerializedName("splashVideoEndMs")
    val splashVideoEndMs: Long = 5000,
    
    @SerializedName("splashLandscape")
    val splashLandscape: Boolean = false,
    
    @SerializedName("splashFillScreen")
    val splashFillScreen: Boolean = true,
    
    @SerializedName("splashEnableAudio")
    val splashEnableAudio: Boolean = false,
    
    // Media app config
    @SerializedName("appType")
    val appType: String = "WEB",
    
    @SerializedName("mediaConfig")
    val mediaConfig: MediaShellConfig = MediaShellConfig(),
    
    // HTML app config
    @SerializedName("htmlConfig")
    val htmlConfig: HtmlShellConfig = HtmlShellConfig(),
    
    // Background music config
    @SerializedName("bgmEnabled")
    val bgmEnabled: Boolean = false,
    
    @SerializedName("bgmPlaylist")
    val bgmPlaylist: List<BgmShellItem> = emptyList(),
    
    @SerializedName("bgmPlayMode")
    val bgmPlayMode: String = "LOOP",
    
    @SerializedName("bgmVolume")
    val bgmVolume: Float = 0.5f,
    
    @SerializedName("bgmAutoPlay")
    val bgmAutoPlay: Boolean = true,
    
    @SerializedName("bgmShowLyrics")
    val bgmShowLyrics: Boolean = true,
    
    @SerializedName("bgmLrcTheme")
    val bgmLrcTheme: LrcShellTheme? = null,
    
    // Theme config
    @SerializedName("themeType")
    val themeType: String = "AURORA",
    
    @SerializedName("darkMode")
    val darkMode: String = "SYSTEM",
    
    // Web page auto-translate config
    @SerializedName("translateEnabled")
    val translateEnabled: Boolean = false,
    
    @SerializedName("translateTargetLanguage")
    val translateTargetLanguage: String = "zh-CN",
    
    @SerializedName("translateShowButton")
    val translateShowButton: Boolean = true,
    
    // Extension module config
    @SerializedName("extensionFabIcon")
    val extensionFabIcon: String = "",
    
    @SerializedName("extensionModuleIds")
    val extensionModuleIds: List<String> = emptyList(),
    
    // Embedded extension modules (embedded during APK export)
    @SerializedName("embeddedExtensionModules")
    val embeddedExtensionModules: List<EmbeddedShellModule> = emptyList(),
    
    // Auto-start config
    @SerializedName("autoStartConfig")
    val autoStartConfig: AutoStartShellConfig? = null,

    // Forced run config
    @SerializedName("forcedRunConfig")
    val forcedRunConfig: ForcedRunConfig? = null,
    
    // Isolation configuration
    @SerializedName("isolationEnabled")
    val isolationEnabled: Boolean = false,
    
    @SerializedName("isolationConfig")
    val isolationConfig: IsolationShellConfig? = null,
    
    // Background run config
    @SerializedName("backgroundRunEnabled")
    val backgroundRunEnabled: Boolean = false,
    
    @SerializedName("backgroundRunConfig")
    val backgroundRunConfig: BackgroundRunShellConfig? = null,
    
    // Advanced tech config (independent module)
    @SerializedName("blackTechConfig")
    val blackTechConfig: com.webtoapp.core.blacktech.BlackTechConfig? = null,
    
    // App disguise config (independent module)
    @SerializedName("disguiseConfig")
    val disguiseConfig: com.webtoapp.core.disguise.DisguiseConfig? = null,
    
    // UI language configuration
    @SerializedName("language")
    val language: String = "ENGLISH",  // ENGLISH, HINDI
    
    // Gallery app config
    @SerializedName("galleryConfig")
    val galleryConfig: GalleryShellConfig = GalleryShellConfig(),
    
    // WordPress app config
    @SerializedName("wordpressConfig")
    val wordpressConfig: WordPressShellConfig = WordPressShellConfig(),
    
    // Node.js app config
    @SerializedName("nodejsConfig")
    val nodejsConfig: NodeJsShellConfig = NodeJsShellConfig(),
    
    // Deep link config
    @SerializedName("deepLinkEnabled")
    val deepLinkEnabled: Boolean = false,
    
    @SerializedName("deepLinkHosts")
    val deepLinkHosts: List<String> = emptyList(),
    
    // PHP app config
    @SerializedName("phpAppConfig")
    val phpAppConfig: PhpAppShellConfig = PhpAppShellConfig(),
    
    // Python app config
    @SerializedName("pythonAppConfig")
    val pythonAppConfig: PythonAppShellConfig = PythonAppShellConfig(),
    
    // Go app config
    @SerializedName("goAppConfig")
    val goAppConfig: GoAppShellConfig = GoAppShellConfig(),
    
    // Multi-site aggregation config
    @SerializedName("multiWebConfig")
    val multiWebConfig: MultiWebShellConfig = MultiWebShellConfig(),
    
    // Cloud SDK config (parameters embedded in exported APK)
    @SerializedName("cloudSdkConfig")
    val cloudSdkConfig: CloudSdkConfig = CloudSdkConfig()
)

/**
 * Extension module data embedded in Shell APK
 */
data class EmbeddedShellModule(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("icon")
    val icon: String = "package",
    
    @SerializedName("category")
    val category: String = "OTHER",
    
    @SerializedName("code")
    val code: String = "",
    
    @SerializedName("cssCode")
    val cssCode: String = "",
    
    @SerializedName("runAt")
    val runAt: String = "DOCUMENT_END",
    
    @SerializedName("urlMatches")
    val urlMatches: List<EmbeddedUrlMatch> = emptyList(),
    
    @SerializedName("configValues")
    val configValues: Map<String, String> = emptyMap(),
    
    @SerializedName("enabled")
    val enabled: Boolean = true
) {
    companion object {
        private val GSON = com.webtoapp.util.GsonProvider.gson
        /** URL matching Regex cache to avoid reconstruction */
        private val regexCache = android.util.LruCache<String, Regex>(32)
    }
    
    /**
     * Check if URL matches this module
     */
    fun matchesUrl(url: String): Boolean {
        if (urlMatches.isEmpty()) return true
        
        val includeRules = urlMatches.filter { !it.exclude }
        val excludeRules = urlMatches.filter { it.exclude }
        
        // Check exclusion rules first
        for (rule in excludeRules) {
            if (matchRule(url, rule)) return false
        }
        
        // If no inclusion rules, match by default
        if (includeRules.isEmpty()) return true
        
        // Check包含规则
        return includeRules.any { matchRule(url, it) }
    }
    
    private fun matchRule(url: String, rule: EmbeddedUrlMatch): Boolean {
        return try {
            val cacheKey = if (rule.isRegex) rule.pattern else "glob:${rule.pattern}"
            val regex = EmbeddedShellModule.regexCache.get(cacheKey) ?: run {
                val r = if (rule.isRegex) {
                    Regex(rule.pattern)
                } else {
                    // Glob matching: * matches any characters
                    val regexPattern = rule.pattern
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".")
                    Regex(regexPattern, RegexOption.IGNORE_CASE)
                }
                EmbeddedShellModule.regexCache.put(cacheKey, r)
                r
            }
            regex.containsMatchIn(url)
        } catch (e: Exception) {
            url.contains(rule.pattern, ignoreCase = true)
        }
    }
    
    @Transient
    @Volatile
    private var _cachedCode: String? = null
    
    /**
     * Generate executable JavaScript code
     * Results are cached to avoid repeated serialization
     */
    fun generateExecutableCode(): String {
        _cachedCode?.let { return it }
        val configJson = EmbeddedShellModule.GSON.toJson(configValues)
        return """
            (function() {
                'use strict';
                // Module config
                const __MODULE_CONFIG__ = $configJson;
                const __MODULE_INFO__ = {
                    id: '${id.escapeForJsSingleQuote()}',
                    name: '${name.escapeForJsSingleQuote()}',
                    version: '1.0.0'
                };
                
                // Configuration access function
                function getConfig(key, defaultValue) {
                    return __MODULE_CONFIG__[key] !== undefined ? __MODULE_CONFIG__[key] : defaultValue;
                }
                
                // CSS Injection
                ${if (cssCode.isNotBlank()) """
                (function() {
                    const style = document.createElement('style');
                    style.id = 'ext-module-${id}';
                    style.textContent = `${cssCode.escapeForJsTemplate()}`;
                    (document.head || document.documentElement).appendChild(style);
                })();
                """ else ""}
                
                // User code
                try {
                    $code
                } catch(e) {
                    console.error('[ExtModule: ${name.escapeForJsSingleQuote()}] Error:', e);
                }
            })();
        """.trimIndent().also { _cachedCode = it }
    }
}

/**
 * Embedded URL matching rules
 */
data class EmbeddedUrlMatch(
    @SerializedName("pattern")
    val pattern: String = "",
    
    @SerializedName("isRegex")
    val isRegex: Boolean = false,
    
    @SerializedName("exclude")
    val exclude: Boolean = false
)

/**
 * Gallery App Shell Config
 */
data class GalleryShellConfig(
    @SerializedName("items")
    val items: List<GalleryShellItem> = emptyList(),
    
    @SerializedName("playMode")
    val playMode: String = "SEQUENTIAL",  // SEQUENTIAL, SHUFFLE, SINGLE_LOOP
    
    @SerializedName("imageInterval")
    val imageInterval: Int = 3,
    
    @SerializedName("loop")
    val loop: Boolean = true,
    
    @SerializedName("autoPlay")
    val autoPlay: Boolean = false,
    
    @SerializedName("backgroundColor")
    val backgroundColor: String = "#000000",
    
    @SerializedName("showThumbnailBar")
    val showThumbnailBar: Boolean = true,
    
    @SerializedName("showMediaInfo")
    val showMediaInfo: Boolean = true,
    
    @SerializedName("orientation")
    val orientation: String = "PORTRAIT",  // PORTRAIT, LANDSCAPE
    
    @SerializedName("enableAudio")
    val enableAudio: Boolean = true,
    
    @SerializedName("videoAutoNext")
    val videoAutoNext: Boolean = true
)

/**
 * Gallery Media Item Shell Config
 */
data class GalleryShellItem(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("assetPath")
    val assetPath: String = "",  // assets/gallery/item_0.{png|mp4}
    
    @SerializedName("type")
    val type: String = "IMAGE",  // IMAGE or VIDEO
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("duration")
    val duration: Long = 0,
    
    @SerializedName("thumbnailPath")
    val thumbnailPath: String? = null  // assets/gallery/thumb_0.jpg
)

/**
 * Media App Shell Config
 */
data class MediaShellConfig(
    @SerializedName("enableAudio")
    val enableAudio: Boolean = true,
    
    @SerializedName("loop")
    val loop: Boolean = true,
    
    @SerializedName("autoPlay")
    val autoPlay: Boolean = true,
    
    @SerializedName("fillScreen")
    val fillScreen: Boolean = true,
    
    @SerializedName("landscape")
    val landscape: Boolean = false,
    
    @SerializedName("keepScreenOn")
    val keepScreenOn: Boolean = true  // Keep screen on
)

/**
 * HTML App Shell Config
 */
/**
 * WordPress App Shell Config
 */
data class WordPressShellConfig(
    @SerializedName("siteTitle")
    val siteTitle: String = "My Site",
    
    @SerializedName("phpPort")
    val phpPort: Int = 0,
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

/**
 * Node.js App Shell Config
 */
data class NodeJsShellConfig(
    @SerializedName("mode")
    val mode: String = "STATIC",  // STATIC, BACKEND, FULLSTACK
    
    @SerializedName("port")
    val port: Int = 0,  // Node.js server port (0 = auto-assign)
    
    @SerializedName("entryFile")
    val entryFile: String = "",  // Entry file (needed for backend/fullstack)
    
    @SerializedName("envVars")
    val envVars: Map<String, String> = emptyMap(),  // Env variables
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

/**
 * HTML App Shell Config
 */
/**
 * PHP App Shell Config
 */
data class PhpAppShellConfig(
    @SerializedName("framework")
    val framework: String = "",
    
    @SerializedName("documentRoot")
    val documentRoot: String = "",
    
    @SerializedName("entryFile")
    val entryFile: String = "index.php",
    
    @SerializedName("port")
    val port: Int = 0,
    
    @SerializedName("envVars")
    val envVars: Map<String, String> = emptyMap(),
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

/**
 * Python App Shell Config
 */
data class PythonAppShellConfig(
    @SerializedName("framework")
    val framework: String = "",
    
    @SerializedName("entryFile")
    val entryFile: String = "app.py",
    
    @SerializedName("entryModule")
    val entryModule: String = "",
    
    @SerializedName("serverType")
    val serverType: String = "builtin",
    
    @SerializedName("port")
    val port: Int = 0,
    
    @SerializedName("envVars")
    val envVars: Map<String, String> = emptyMap(),
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

/**
 * Go App Shell Config
 */
data class GoAppShellConfig(
    @SerializedName("framework")
    val framework: String = "",
    
    @SerializedName("binaryName")
    val binaryName: String = "",
    
    @SerializedName("port")
    val port: Int = 0,
    
    @SerializedName("staticDir")
    val staticDir: String = "",
    
    @SerializedName("envVars")
    val envVars: Map<String, String> = emptyMap(),
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

/**
 * Multi-site aggregation Shell configuration
 */
data class MultiWebShellConfig(
    @SerializedName("sites")
    val sites: List<MultiWebSiteShellConfig> = emptyList(),
    
    @SerializedName("displayMode")
    val displayMode: String = "TABS",
    
    @SerializedName("refreshInterval")
    val refreshInterval: Int = 30,
    
    @SerializedName("showSiteIcons")
    val showSiteIcons: Boolean = true,
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
)

data class MultiWebSiteShellConfig(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("url")
    val url: String = "",
    
    @SerializedName("iconEmoji")
    val iconEmoji: String = "",
    
    @SerializedName("category")
    val category: String = "",
    
    @SerializedName("cssSelector")
    val cssSelector: String = "",
    
    @SerializedName("linkSelector")
    val linkSelector: String = "",
    
    @SerializedName("enabled")
    val enabled: Boolean = true
)

data class HtmlShellConfig(
    @SerializedName("entryFile")
    val entryFile: String = "index.html",
    
    @SerializedName("enableJavaScript")
    val enableJavaScript: Boolean = true,
    
    @SerializedName("enableLocalStorage")
    val enableLocalStorage: Boolean = true,
    
    @SerializedName("backgroundColor")
    val backgroundColor: String = "#FFFFFF",
    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false
) {
    /**
     * Get valid entry file name
     * Verifies entryFile must have a filename part (cannot be just .html or empty)
     */
    fun getValidEntryFile(): String {
        return entryFile.takeIf { 
            it.isNotBlank() && it.substringBeforeLast(".").isNotBlank() 
        } ?: "index.html"
    }
}

/**
 * WebView Shell configuration
 */
data class WebViewShellConfig(
    @SerializedName("javaScriptEnabled")
    val javaScriptEnabled: Boolean = true,

    @SerializedName("domStorageEnabled")
    val domStorageEnabled: Boolean = true,

    @SerializedName("zoomEnabled")
    val zoomEnabled: Boolean = true,

    @SerializedName("desktopMode")
    val desktopMode: Boolean = false,

    @SerializedName("userAgent")
    val userAgent: String? = null,
    
    @SerializedName("userAgentMode")
    val userAgentMode: String = "DEFAULT",
    
    @SerializedName("customUserAgent")
    val customUserAgent: String? = null,

    @SerializedName("hideToolbar")
    val hideToolbar: Boolean = false,

    @SerializedName("hideBrowserToolbar")
    val hideBrowserToolbar: Boolean = false,
    
    @SerializedName("showStatusBarInFullscreen")
    val showStatusBarInFullscreen: Boolean = false,  // Whether to show status bar in Fullscreen mode    
    @SerializedName("showNavigationBarInFullscreen")
    val showNavigationBarInFullscreen: Boolean = false,  // Whether to show navigation bar in Fullscreen mode    
    @SerializedName("showToolbarInFullscreen")
    val showToolbarInFullscreen: Boolean = false,  // Whether to show top toolbar in Fullscreen mode    
    @SerializedName("landscapeMode")
    val landscapeMode: Boolean = false,
    
    @SerializedName("orientationMode")
    val orientationMode: String = "PORTRAIT", // PORTRAIT, LANDSCAPE, REVERSE_PORTRAIT, REVERSE_LANDSCAPE, SENSOR_PORTRAIT, SENSOR_LANDSCAPE, AUTO
    
    @SerializedName("injectScripts")
    val injectScripts: List<ShellUserScript> = emptyList(),
    
    @SerializedName("statusBarColorMode")
    val statusBarColorMode: String = "THEME", // THEME, TRANSPARENT, CUSTOM
    
    @SerializedName("statusBarColor")
    val statusBarColor: String? = null, // Custom status bar color (only in CUSTOM mode)
    
    @SerializedName("statusBarDarkIcons")
    val statusBarDarkIcons: Boolean? = null, // Status bar icon color: true=dark icons, false=light icons, null=auto
    
    @SerializedName("statusBarBackgroundType")
    val statusBarBackgroundType: String = "COLOR", // COLOR, IMAGE
    
    @SerializedName("statusBarBackgroundImage")
    val statusBarBackgroundImage: String? = null, // Cropped image path (assets path)
    
    @SerializedName("statusBarBackgroundAlpha")
    val statusBarBackgroundAlpha: Float = 1.0f, // Alpha 0.0-1.0
    
    @SerializedName("statusBarHeightDp")
    val statusBarHeightDp: Int = 0, // Custom height dp (0 = system default)

    // Status bar dark mode config
    @SerializedName("statusBarColorModeDark")
    val statusBarColorModeDark: String = "THEME",

    @SerializedName("statusBarColorDark")
    val statusBarColorDark: String? = null,

    @SerializedName("statusBarDarkIconsDark")
    val statusBarDarkIconsDark: Boolean? = null,

    @SerializedName("statusBarBackgroundTypeDark")
    val statusBarBackgroundTypeDark: String = "COLOR",

    @SerializedName("statusBarBackgroundImageDark")
    val statusBarBackgroundImageDark: String? = null,

    @SerializedName("statusBarBackgroundAlphaDark")
    val statusBarBackgroundAlphaDark: Float = 1.0f,

    @SerializedName("longPressMenuEnabled")
    val longPressMenuEnabled: Boolean = true, // Enable long-press menu
    
    @SerializedName("longPressMenuStyle")
    val longPressMenuStyle: String = "FULL", // DISABLED, SIMPLE, FULL
    
    @SerializedName("adBlockToggleEnabled")
    val adBlockToggleEnabled: Boolean = false, // Allow users to toggle ad blocking at runtime    
    @SerializedName("popupBlockerEnabled")
    val popupBlockerEnabled: Boolean = true, // Enable popup blocker    
    @SerializedName("popupBlockerToggleEnabled")
    val popupBlockerToggleEnabled: Boolean = false, // Allow users to toggle popup blocking at runtime    
    @SerializedName("openExternalLinks")
    val openExternalLinks: Boolean = false, // Whether to open external links in browser    
    // ============ Browser compatibility enhancement configuration ============
    @SerializedName("initialScale")
    val initialScale: Int = 0,
    
    @SerializedName("viewportMode")
    val viewportMode: String = "DEFAULT", // DEFAULT, FIT_SCREEN, DESKTOP
    
    @SerializedName("newWindowBehavior")
    val newWindowBehavior: String = "SAME_WINDOW",
    
    @SerializedName("enablePaymentSchemes")
    val enablePaymentSchemes: Boolean = true,
    
    @SerializedName("enableShareBridge")
    val enableShareBridge: Boolean = true,
    
    @SerializedName("enableZoomPolyfill")
    val enableZoomPolyfill: Boolean = true,
    
    // ============ Advanced features configuration ============
    @SerializedName("enableCrossOriginIsolation")
    val enableCrossOriginIsolation: Boolean = false,
    
    @SerializedName("disableShields")
    val disableShields: Boolean = true, // Disable BrowserShields by default - avoid false positives for OAuth/CAPTCHA etc    
    @SerializedName("keepScreenOn")
    val keepScreenOn: Boolean = false, // [Backward compatibility] Keep screen on    
    @SerializedName("screenAwakeMode")
    val screenAwakeMode: String = "OFF", // OFF, ALWAYS, TIMED
    
    @SerializedName("screenAwakeTimeoutMinutes")
    val screenAwakeTimeoutMinutes: Int = 30, // Timeout in minutes, only in TIMED mode    
    @SerializedName("screenBrightness")
    val screenBrightness: Int = -1, // Screen brightness: -1=system, 0-100=percentage    
    // ============ Floating back button ============
    @SerializedName("showFloatingBackButton")
    val showFloatingBackButton: Boolean = true, // Whether to show floating back button in Fullscreen mode
    // ============ Block system navigation gesture ============
    @SerializedName("blockSystemNavigationGesture")
    val blockSystemNavigationGesture: Boolean = false, // Whether to block system navigation gestures (only in Fullscreen)
    // ============ Keyboard adjust mode ============
    @SerializedName("keyboardAdjustMode")
    val keyboardAdjustMode: String = "RESIZE", // RESIZE, NOTHING

    // ============ Pull-to-refresh / Video fullscreen ============
    @SerializedName("swipeRefreshEnabled")
    val swipeRefreshEnabled: Boolean = true,
    
    @SerializedName("fullscreenEnabled")
    val fullscreenEnabled: Boolean = true,

    // ============ Performance optimization / PWA offline ============
    @SerializedName("performanceOptimization")
    val performanceOptimization: Boolean = false,
    
    @SerializedName("pwaOfflineEnabled")
    val pwaOfflineEnabled: Boolean = false,
    
    @SerializedName("pwaOfflineStrategy")
    val pwaOfflineStrategy: String = "NETWORK_FIRST",

    // ============ Network error page config ============
    @SerializedName("errorPageConfig")
    val errorPageConfig: ErrorPageShellConfig = ErrorPageShellConfig(),

    // ============ Floating window configuration ============
    @SerializedName("floatingWindowConfig")
    val floatingWindowConfig: FloatingWindowShellConfig = FloatingWindowShellConfig()
)

/**
 * Floating window Shell configuration
 */
data class FloatingWindowShellConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    
    @SerializedName("windowSizePercent")
    val windowSizePercent: Int = 80,       // [Backward compatibility] Window size percentage 50-100    
    @SerializedName("widthPercent")
    val widthPercent: Int = 80,            // Independent width percentage 30-100    
    @SerializedName("heightPercent")
    val heightPercent: Int = 80,           // Independent height percentage 30-100    
    @SerializedName("lockAspectRatio")
    val lockAspectRatio: Boolean = true,   // 锁定宽高比
    
    @SerializedName("opacity")
    val opacity: Int = 100,                 // 透明度百分比 30-100
    
    @SerializedName("cornerRadius")
    val cornerRadius: Int = 16,             // Corner radius dp (0-32)    
    @SerializedName("borderStyle")
    val borderStyle: String = "SUBTLE",     // NONE, SUBTLE, GLOW, ACCENT
    
    @SerializedName("showTitleBar")
    val showTitleBar: Boolean = true,       // 显示标题栏
    
    @SerializedName("autoHideTitleBar")
    val autoHideTitleBar: Boolean = false,  // 自动隐藏标题栏
    
    @SerializedName("startMinimized")
    val startMinimized: Boolean = false,    // 启动时最小化
    
    @SerializedName("rememberPosition")
    val rememberPosition: Boolean = true,   // 记住位置
    
    @SerializedName("edgeSnapping")
    val edgeSnapping: Boolean = true,       // 边缘吸附
    
    @SerializedName("showResizeHandle")
    val showResizeHandle: Boolean = true,   // 显示缩放手柄
    
    @SerializedName("lockPosition")
    val lockPosition: Boolean = false       // 锁定位置
)

/**
 * Network error page Shell configuration
 */
data class ErrorPageShellConfig(
    @SerializedName("mode")
    val mode: String = "BUILTIN_STYLE", // DEFAULT, BUILTIN_STYLE, CUSTOM_HTML, CUSTOM_MEDIA
    
    @SerializedName("builtInStyle")
    val builtInStyle: String = "MATERIAL",
    
    @SerializedName("showMiniGame")
    val showMiniGame: Boolean = false,
    
    @SerializedName("miniGameType")
    val miniGameType: String = "RANDOM",
    
    @SerializedName("autoRetrySeconds")
    val autoRetrySeconds: Int = 15
)

/**
 * Shell mode user script configuration
 */
data class ShellUserScript(
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("code")
    val code: String = "",
    
    @SerializedName("enabled")
    val enabled: Boolean = true,
    
    @SerializedName("runAt")
    val runAt: String = "DOCUMENT_END"
)

/**
 * BGM Item (for Shell config)
 */
data class BgmShellItem(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("assetPath")
    val assetPath: String = "",
    
    @SerializedName("lrcAssetPath")
    val lrcAssetPath: String? = null,
    
    @SerializedName("sortOrder")
    val sortOrder: Int = 0
)

/**
 * Lyric theme (for Shell config)
 */
data class LrcShellTheme(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("fontSize")
    val fontSize: Float = 18f,
    
    @SerializedName("textColor")
    val textColor: String = "#FFFFFF",
    
    @SerializedName("highlightColor")
    val highlightColor: String = "#FFD700",
    
    @SerializedName("backgroundColor")
    val backgroundColor: String = "#80000000",
    
    @SerializedName("animationType")
    val animationType: String = "FADE",
    
    @SerializedName("position")
    val position: String = "BOTTOM"
)

/**
 * Auto-start configuration (for Shell config)
 */
data class AutoStartShellConfig(
    @SerializedName("bootStartEnabled")
    val bootStartEnabled: Boolean = false,
    
    @SerializedName("scheduledStartEnabled")
    val scheduledStartEnabled: Boolean = false,
    
    @SerializedName("scheduledTime")
    val scheduledTime: String = "08:00",
    
    @SerializedName("scheduledDays")
    val scheduledDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
)

/**
 * Isolation Environment / Multi-app Configuration (for Shell config)
 */
data class IsolationShellConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    
    @SerializedName("fingerprintConfig")
    val fingerprintConfig: FingerprintShellConfig = FingerprintShellConfig(),
    
    @SerializedName("headerConfig")
    val headerConfig: HeaderShellConfig = HeaderShellConfig(),
    
    @SerializedName("ipSpoofConfig")
    val ipSpoofConfig: IpSpoofShellConfig = IpSpoofShellConfig(),
    
    @SerializedName("storageIsolation")
    val storageIsolation: Boolean = true,
    
    @SerializedName("blockWebRTC")
    val blockWebRTC: Boolean = true,
    
    @SerializedName("protectCanvas")
    val protectCanvas: Boolean = true,
    
    @SerializedName("protectAudio")
    val protectAudio: Boolean = true,
    
    @SerializedName("protectWebGL")
    val protectWebGL: Boolean = true,
    
    @SerializedName("protectFonts")
    val protectFonts: Boolean = false,
    
    @SerializedName("spoofTimezone")
    val spoofTimezone: Boolean = false,
    
    @SerializedName("customTimezone")
    val customTimezone: String? = null,
    
    @SerializedName("spoofLanguage")
    val spoofLanguage: Boolean = false,
    
    @SerializedName("customLanguage")
    val customLanguage: String? = null,
    
    @SerializedName("spoofScreen")
    val spoofScreen: Boolean = false,
    
    @SerializedName("customScreenWidth")
    val customScreenWidth: Int? = null,
    
    @SerializedName("customScreenHeight")
    val customScreenHeight: Int? = null
) {
    /**
     * Convert to IsolationConfig
     */
    fun toIsolationConfig(): com.webtoapp.core.isolation.IsolationConfig {
        return com.webtoapp.core.isolation.IsolationConfig(
            enabled = enabled,
            fingerprintConfig = com.webtoapp.core.isolation.FingerprintConfig(
                randomize = fingerprintConfig.randomize,
                regenerateOnLaunch = fingerprintConfig.regenerateOnLaunch,
                customUserAgent = fingerprintConfig.customUserAgent,
                randomUserAgent = fingerprintConfig.randomUserAgent,
                fingerprintId = fingerprintConfig.fingerprintId
            ),
            headerConfig = com.webtoapp.core.isolation.HeaderConfig(
                enabled = headerConfig.enabled,
                randomizeOnRequest = headerConfig.randomizeOnRequest,
                dnt = headerConfig.dnt,
                spoofClientHints = headerConfig.spoofClientHints,
                refererPolicy = try {
                    com.webtoapp.core.isolation.RefererPolicy.valueOf(headerConfig.refererPolicy)
                } catch (e: Exception) {
                    com.webtoapp.core.isolation.RefererPolicy.STRICT_ORIGIN
                }
            ),
            ipSpoofConfig = com.webtoapp.core.isolation.IpSpoofConfig(
                enabled = ipSpoofConfig.enabled,
                spoofMethod = try {
                    com.webtoapp.core.isolation.IpSpoofMethod.valueOf(ipSpoofConfig.spoofMethod)
                } catch (e: Exception) {
                    com.webtoapp.core.isolation.IpSpoofMethod.HEADER
                },
                customIp = ipSpoofConfig.customIp,
                randomIpRange = try {
                    com.webtoapp.core.isolation.IpRange.valueOf(ipSpoofConfig.randomIpRange)
                } catch (e: Exception) {
                    com.webtoapp.core.isolation.IpRange.GLOBAL
                },
                searchKeyword = ipSpoofConfig.searchKeyword,
                xForwardedFor = ipSpoofConfig.xForwardedFor,
                xRealIp = ipSpoofConfig.xRealIp,
                clientIp = ipSpoofConfig.clientIp
            ),
            storageIsolation = storageIsolation,
            blockWebRTC = blockWebRTC,
            protectCanvas = protectCanvas,
            protectAudio = protectAudio,
            protectWebGL = protectWebGL,
            protectFonts = protectFonts,
            spoofTimezone = spoofTimezone,
            customTimezone = customTimezone,
            spoofLanguage = spoofLanguage,
            customLanguage = customLanguage,
            spoofScreen = spoofScreen,
            customScreenWidth = customScreenWidth,
            customScreenHeight = customScreenHeight
        )
    }
}

/**
 * Fingerprint configuration (for Shell config)
 */
data class FingerprintShellConfig(
    @SerializedName("randomize")
    val randomize: Boolean = true,
    
    @SerializedName("regenerateOnLaunch")
    val regenerateOnLaunch: Boolean = false,
    
    @SerializedName("customUserAgent")
    val customUserAgent: String? = null,
    
    @SerializedName("randomUserAgent")
    val randomUserAgent: Boolean = true,
    
    @SerializedName("fingerprintId")
    val fingerprintId: String = java.util.UUID.randomUUID().toString()
)

/**
 * Header configuration (for Shell config)
 */
data class HeaderShellConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    
    @SerializedName("randomizeOnRequest")
    val randomizeOnRequest: Boolean = false,
    
    @SerializedName("dnt")
    val dnt: Boolean = true,
    
    @SerializedName("spoofClientHints")
    val spoofClientHints: Boolean = true,
    
    @SerializedName("refererPolicy")
    val refererPolicy: String = "STRICT_ORIGIN"
)

/**
 * IP Spoofing configuration (for Shell config)
 */
data class IpSpoofShellConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    
    @SerializedName("spoofMethod")
    val spoofMethod: String = "HEADER",
    
    @SerializedName("customIp")
    val customIp: String? = null,
    
    @SerializedName("randomIpRange")
    val randomIpRange: String = "GLOBAL",
    
    @SerializedName("searchKeyword")
    val searchKeyword: String? = null,
    
    @SerializedName("xForwardedFor")
    val xForwardedFor: Boolean = true,
    
    @SerializedName("xRealIp")
    val xRealIp: Boolean = true,
    
    @SerializedName("clientIp")
    val clientIp: Boolean = true
)

/**
 * Background run configuration (for Shell config)
 */
data class BackgroundRunShellConfig(
    @SerializedName("notificationTitle")
    val notificationTitle: String = "",
    
    @SerializedName("notificationContent")
    val notificationContent: String = "",
    
    @SerializedName("showNotification")
    val showNotification: Boolean = true,
    
    @SerializedName("keepCpuAwake")
    val keepCpuAwake: Boolean = true
)







