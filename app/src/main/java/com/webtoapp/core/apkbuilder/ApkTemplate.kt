 package com.webtoapp.core.apkbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.util.GsonProvider
import com.webtoapp.core.shell.BgmShellItem
import com.webtoapp.core.shell.LrcShellTheme
import java.io.*
import java.util.zip.*/**
 * Note: brief English comment.
 * Note: brief English comment.
 */
class ApkTemplate(private val context: Context) {

    companion object {
        // Note: brief English comment.
        private const val TEMPLATE_APK = "template/webview_shell.apk"
        
        // Note: brief English comment.
        const val CONFIG_PATH = "assets/app_config.json"
        
        // Note: brief English comment.
        val ICON_PATHS = listOf(
            "res/mipmap-mdpi-v4/ic_launcher.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher.png" to 192
        )
        
        // Note: brief English comment.
        val ROUND_ICON_PATHS = listOf(
            "res/mipmap-mdpi-v4/ic_launcher_round.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher_round.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher_round.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher_round.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher_round.png" to 192
        )
    }

    private val gson = GsonProvider.gson

    // Note: brief English comment.
    private val templateDir = File(context.cacheDir, "apk_templates")

    init {
        templateDir.mkdirs()
    }

    /**
     * Note: brief English comment.
     * Note: brief English comment.
     */
    fun getTemplateApk(): File? {
        val templateFile = File(templateDir, "webview_shell.apk")
        
        // Note: brief English comment.
        if (templateFile.exists()) {
            return templateFile
        }

        // Note: brief English comment.
        return try {
            context.assets.open(TEMPLATE_APK).use { input ->
                FileOutputStream(templateFile).use { output ->
                    input.copyTo(output)
                }
            }
            templateFile
        } catch (e: Exception) {
            // Note: brief English comment.
            null
        }
    }

    /**
     * Note: brief English comment.
     */
    fun hasTemplate(): Boolean {
        return try {
            context.assets.open(TEMPLATE_APK).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Note: brief English comment.
     */
    fun createConfigJson(config: ApkConfig): String {
        val root = mutableMapOf<String, Any?>()
        
        root["appName"] = config.appName
        root["packageName"] = config.packageName
        root["targetUrl"] = config.targetUrl
        root["versionCode"] = config.versionCode
        root["versionName"] = config.versionName
        root["activationEnabled"] = config.activationEnabled
        root["activationCodes"] = config.activationCodes
        root["activationRequireEveryTime"] = config.activationRequireEveryTime
        root["activationDialogTitle"] = config.activationDialogTitle
        root["activationDialogSubtitle"] = config.activationDialogSubtitle
        root["activationDialogInputLabel"] = config.activationDialogInputLabel
        root["activationDialogButtonText"] = config.activationDialogButtonText
        root["adBlockEnabled"] = config.adBlockEnabled
        root["adBlockRules"] = config.adBlockRules
        root["announcementEnabled"] = config.announcementEnabled
        root["announcementTitle"] = config.announcementTitle
        root["announcementContent"] = config.announcementContent
        root["announcementLink"] = config.announcementLink
        root["announcementLinkText"] = config.announcementLinkText
        root["announcementTemplate"] = config.announcementTemplate
        root["announcementShowEmoji"] = config.announcementShowEmoji
        root["announcementAnimationEnabled"] = config.announcementAnimationEnabled
        root["announcementShowOnce"] = config.announcementShowOnce
        root["announcementRequireConfirmation"] = config.announcementRequireConfirmation
        root["announcementAllowNeverShow"] = config.announcementAllowNeverShow
        root["splashEnabled"] = config.splashEnabled
        root["splashType"] = config.splashType
        root["splashDuration"] = config.splashDuration
        root["splashClickToSkip"] = config.splashClickToSkip
        root["splashVideoStartMs"] = config.splashVideoStartMs
        root["splashVideoEndMs"] = config.splashVideoEndMs
        root["splashLandscape"] = config.splashLandscape
        root["splashFillScreen"] = config.splashFillScreen
        root["splashEnableAudio"] = config.splashEnableAudio
        
        val webViewConfig = mutableMapOf<String, Any?>()
        webViewConfig["javaScriptEnabled"] = config.javaScriptEnabled
        webViewConfig["domStorageEnabled"] = config.domStorageEnabled
        webViewConfig["zoomEnabled"] = config.zoomEnabled
        webViewConfig["desktopMode"] = config.desktopMode
        webViewConfig["userAgent"] = config.userAgent
        webViewConfig["userAgentMode"] = config.userAgentMode
        webViewConfig["customUserAgent"] = config.customUserAgent
        webViewConfig["orientationMode"] = config.orientationMode
        webViewConfig["keyboardAdjustMode"] = config.keyboardAdjustMode
        webViewConfig["swipeRefreshEnabled"] = config.swipeRefreshEnabled
        webViewConfig["fullscreenEnabled"] = config.fullscreenEnabled
        webViewConfig["hideToolbar"] = config.hideToolbar
        webViewConfig["hideBrowserToolbar"] = config.hideBrowserToolbar
        webViewConfig["showStatusBarInFullscreen"] = config.showStatusBarInFullscreen
        webViewConfig["showNavigationBarInFullscreen"] = config.showNavigationBarInFullscreen
        webViewConfig["showToolbarInFullscreen"] = config.showToolbarInFullscreen
        webViewConfig["landscapeMode"] = config.landscapeMode
        webViewConfig["injectScripts"] = config.injectScripts.map { script ->
            mapOf(
                "name" to script.name,
                "code" to script.code,
                "enabled" to script.enabled,
                "runAt" to script.runAt.name
            )
        }
        webViewConfig["statusBarColorMode"] = config.statusBarColorMode
        webViewConfig["statusBarColor"] = config.statusBarColor
        webViewConfig["statusBarDarkIcons"] = config.statusBarDarkIcons
        webViewConfig["statusBarBackgroundType"] = config.statusBarBackgroundType
        webViewConfig["statusBarBackgroundImage"] = if (config.statusBarBackgroundType == "IMAGE" && !config.statusBarBackgroundImage.isNullOrEmpty()) "statusbar_background.png" else null
        webViewConfig["statusBarBackgroundAlpha"] = config.statusBarBackgroundAlpha
        webViewConfig["statusBarHeightDp"] = config.statusBarHeightDp
        webViewConfig["statusBarColorModeDark"] = config.statusBarColorModeDark
        webViewConfig["statusBarColorDark"] = config.statusBarColorDark
        webViewConfig["statusBarDarkIconsDark"] = config.statusBarDarkIconsDark
        webViewConfig["statusBarBackgroundTypeDark"] = config.statusBarBackgroundTypeDark
        webViewConfig["statusBarBackgroundImageDark"] = if (config.statusBarBackgroundTypeDark == "IMAGE" && !config.statusBarBackgroundImageDark.isNullOrEmpty()) "statusbar_background_dark.png" else null
        webViewConfig["statusBarBackgroundAlphaDark"] = config.statusBarBackgroundAlphaDark
        webViewConfig["longPressMenuEnabled"] = config.longPressMenuEnabled
        webViewConfig["longPressMenuStyle"] = config.longPressMenuStyle
        webViewConfig["adBlockToggleEnabled"] = config.adBlockToggleEnabled
        webViewConfig["popupBlockerEnabled"] = config.popupBlockerEnabled
        webViewConfig["popupBlockerToggleEnabled"] = config.popupBlockerToggleEnabled
        webViewConfig["openExternalLinks"] = config.openExternalLinks
        webViewConfig["initialScale"] = config.initialScale
        webViewConfig["viewportMode"] = config.viewportMode
        webViewConfig["newWindowBehavior"] = config.newWindowBehavior
        webViewConfig["enablePaymentSchemes"] = config.enablePaymentSchemes
        webViewConfig["enableShareBridge"] = config.enableShareBridge
        webViewConfig["enableZoomPolyfill"] = config.enableZoomPolyfill
        webViewConfig["enableCrossOriginIsolation"] = config.enableCrossOriginIsolation
        webViewConfig["disableShields"] = config.disableShields
        webViewConfig["keepScreenOn"] = config.keepScreenOn
        webViewConfig["screenAwakeMode"] = config.screenAwakeMode
        webViewConfig["screenAwakeTimeoutMinutes"] = config.screenAwakeTimeoutMinutes
        webViewConfig["screenBrightness"] = config.screenBrightness
        webViewConfig["performanceOptimization"] = config.performanceOptimization
        webViewConfig["pwaOfflineEnabled"] = config.pwaOfflineEnabled
        webViewConfig["pwaOfflineStrategy"] = config.pwaOfflineStrategy
        webViewConfig["showFloatingBackButton"] = config.showFloatingBackButton
        
        val floatingWindowConfig = mutableMapOf<String, Any?>()
        floatingWindowConfig["enabled"] = config.floatingWindowEnabled
        floatingWindowConfig["windowSizePercent"] = config.floatingWindowSizePercent
        floatingWindowConfig["widthPercent"] = config.floatingWindowWidthPercent
        floatingWindowConfig["heightPercent"] = config.floatingWindowHeightPercent
        floatingWindowConfig["lockAspectRatio"] = config.floatingWindowLockAspectRatio
        floatingWindowConfig["opacity"] = config.floatingWindowOpacity
        floatingWindowConfig["cornerRadius"] = config.floatingWindowCornerRadius
        floatingWindowConfig["borderStyle"] = config.floatingWindowBorderStyle
        floatingWindowConfig["showTitleBar"] = config.floatingWindowShowTitleBar
        floatingWindowConfig["autoHideTitleBar"] = config.floatingWindowAutoHideTitleBar
        floatingWindowConfig["startMinimized"] = config.floatingWindowStartMinimized
        floatingWindowConfig["rememberPosition"] = config.floatingWindowRememberPosition
        floatingWindowConfig["edgeSnapping"] = config.floatingWindowEdgeSnapping
        floatingWindowConfig["showResizeHandle"] = config.floatingWindowShowResizeHandle
        floatingWindowConfig["lockPosition"] = config.floatingWindowLockPosition
        webViewConfig["floatingWindowConfig"] = floatingWindowConfig
        
        val errorPageConfig = mutableMapOf<String, Any?>()
        errorPageConfig["mode"] = config.errorPageMode
        errorPageConfig["builtInStyle"] = config.errorPageBuiltInStyle
        errorPageConfig["showMiniGame"] = config.errorPageShowMiniGame
        errorPageConfig["miniGameType"] = config.errorPageMiniGameType
        errorPageConfig["autoRetrySeconds"] = config.errorPageAutoRetrySeconds
        webViewConfig["errorPageConfig"] = errorPageConfig
        
        root["webViewConfig"] = webViewConfig
        root["appType"] = config.appType
        root["mediaConfig"] = mapOf(
            "enableAudio" to config.mediaEnableAudio,
            "loop" to config.mediaLoop,
            "autoPlay" to config.mediaAutoPlay,
            "fillScreen" to config.mediaFillScreen,
            "landscape" to config.mediaLandscape,
            "keepScreenOn" to config.mediaKeepScreenOn
        )
        root["htmlConfig"] = mapOf(
            "entryFile" to config.htmlEntryFile,
            "enableJavaScript" to config.htmlEnableJavaScript,
            "enableLocalStorage" to config.htmlEnableLocalStorage,
            "landscapeMode" to config.htmlLandscapeMode
        )
        root["galleryConfig"] = mapOf(
            "items" to config.galleryItems.map { item ->
                mapOf(
                    "id" to item.id,
                    "assetPath" to item.assetPath,
                    "type" to item.type,
                    "name" to item.name,
                    "duration" to item.duration,
                    "thumbnailPath" to item.thumbnailPath
                )
            },
            "playMode" to config.galleryPlayMode,
            "imageInterval" to config.galleryImageInterval,
            "loop" to config.galleryLoop,
            "autoPlay" to config.galleryAutoPlay,
            "backgroundColor" to config.galleryBackgroundColor,
            "showThumbnailBar" to config.galleryShowThumbnailBar,
            "showMediaInfo" to config.galleryShowMediaInfo,
            "orientation" to config.galleryOrientation,
            "enableAudio" to config.galleryEnableAudio,
            "videoAutoNext" to config.galleryVideoAutoNext
        )
        root["bgmEnabled"] = config.bgmEnabled
        root["bgmPlaylist"] = config.bgmPlaylist
        root["bgmPlayMode"] = config.bgmPlayMode
        root["bgmVolume"] = config.bgmVolume
        root["bgmAutoPlay"] = config.bgmAutoPlay
        root["bgmShowLyrics"] = config.bgmShowLyrics
        root["bgmLrcTheme"] = config.bgmLrcTheme
        root["themeType"] = config.themeType
        root["darkMode"] = config.darkMode
        root["translateEnabled"] = config.translateEnabled
        root["translateTargetLanguage"] = config.translateTargetLanguage
        root["translateShowButton"] = config.translateShowButton
        root["extensionFabIcon"] = config.extensionFabIcon
        root["extensionModuleIds"] = config.extensionModuleIds
        root["embeddedExtensionModules"] = config.embeddedExtensionModules
        root["autoStartConfig"] = if (config.bootStartEnabled || config.scheduledStartEnabled) {
            mapOf(
                "bootStartEnabled" to config.bootStartEnabled,
                "scheduledStartEnabled" to config.scheduledStartEnabled,
                "scheduledTime" to config.scheduledTime,
                "scheduledDays" to config.scheduledDays
            )
        } else null
        root["forcedRunConfig"] = config.forcedRunConfig
        root["isolationEnabled"] = config.isolationEnabled
        root["isolationConfig"] = config.isolationConfig
        root["backgroundRunEnabled"] = config.backgroundRunEnabled
        root["backgroundRunConfig"] = config.backgroundRunConfig
        root["blackTechConfig"] = config.blackTechConfig
        root["disguiseConfig"] = config.disguiseConfig
        root["language"] = config.language
        root["engineType"] = config.engineType
        root["deepLinkEnabled"] = config.deepLinkEnabled
        root["deepLinkHosts"] = config.deepLinkHosts
        root["wordpressConfig"] = mapOf(
            "siteTitle" to config.wordpressSiteTitle,
            "phpPort" to config.wordpressPhpPort,
            "landscapeMode" to config.wordpressLandscapeMode
        )
        root["nodejsConfig"] = mapOf(
            "mode" to config.nodejsMode,
            "port" to config.nodejsPort,
            "entryFile" to config.nodejsEntryFile,
            "envVars" to config.nodejsEnvVars,
            "landscapeMode" to config.nodejsLandscapeMode
        )
        root["phpAppConfig"] = mapOf(
            "framework" to config.phpAppFramework,
            "documentRoot" to config.phpAppDocumentRoot,
            "entryFile" to config.phpAppEntryFile,
            "port" to config.phpAppPort,
            "envVars" to config.phpAppEnvVars,
            "landscapeMode" to config.phpAppLandscapeMode
        )
        root["pythonAppConfig"] = mapOf(
            "framework" to config.pythonAppFramework,
            "entryFile" to config.pythonAppEntryFile,
            "entryModule" to config.pythonAppEntryModule,
            "serverType" to config.pythonAppServerType,
            "port" to config.pythonAppPort,
            "envVars" to config.pythonAppEnvVars,
            "landscapeMode" to config.pythonAppLandscapeMode
        )
        root["goAppConfig"] = mapOf(
            "framework" to config.goAppFramework,
            "binaryName" to config.goAppBinaryName,
            "port" to config.goAppPort,
            "staticDir" to config.goAppStaticDir,
            "envVars" to config.goAppEnvVars,
            "landscapeMode" to config.goAppLandscapeMode
        )
        root["multiWebConfig"] = mapOf(
            "sites" to config.multiWebSites,
            "displayMode" to config.multiWebDisplayMode,
            "refreshInterval" to config.multiWebRefreshInterval,
            "showSiteIcons" to config.multiWebShowSiteIcons,
            "landscapeMode" to config.multiWebLandscapeMode
        )
        root["cloudSdkConfig"] = config.cloudSdkConfig
        
        return gson.toJson(root)
    }

    /**
     * Note: brief English comment.
     * Note: brief English comment.
     */
    private fun escapeJson(str: String): String {
        val sb = StringBuilder()
        for (char in str) {
            when (char) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f") // form feed
                else -> {
                    // Note: brief English comment.
                    if (char.code < 32) {
                        sb.append("\\u${char.code.toString(16).padStart(4, '0')}")
                    } else {
                        sb.append(char)
                    }
                }
            }
        }
        return sb.toString()
    }

    /**
     * Note: brief English comment.
     */
    fun scaleBitmapToPng(bitmap: Bitmap, size: Int): ByteArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, baos)
        if (scaled != bitmap) {
            scaled.recycle()
        }
        return baos.toByteArray()
    }

    /**
     * Note: brief English comment.
     */
    fun loadBitmap(iconPath: String): Bitmap? {
        return try {
            if (iconPath.startsWith("/")) {
                BitmapFactory.decodeFile(iconPath)
            } else if (iconPath.startsWith("content://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(iconPath))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                BitmapFactory.decodeFile(iconPath)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Note: brief English comment.
     * Note: brief English comment.
     * Note: brief English comment.
     * Note: brief English comment.
     * Note: brief English comment.
     *
     * Note: brief English comment.
     * Note: brief English comment.
     * Note: brief English comment.
     */
    fun createAdaptiveForegroundIcon(bitmap: Bitmap, size: Int): ByteArray {
        // Note: brief English comment.
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        // Note: brief English comment.
        val safeZoneSize = (size * 72f / 108f).toInt()
        val padding = (size - safeZoneSize) / 2
        
        // Note: brief English comment.
        val scaled = Bitmap.createScaledBitmap(bitmap, safeZoneSize, safeZoneSize, true)
        
        // Note: brief English comment.
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(scaled, padding.toFloat(), padding.toFloat(), paint)
        
        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        
        if (scaled != bitmap) scaled.recycle()
        output.recycle()
        
        return baos.toByteArray()
    }

    /**
     * Note: brief English comment.
     */
    fun createRoundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        
        // Note: brief English comment.
        val rect = android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        
        // Note: brief English comment.
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        
        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        
        if (scaled != bitmap) scaled.recycle()
        output.recycle()
        
        return baos.toByteArray()
    }

    /**
     * Note: brief English comment.
     */
    fun clearCache() {
        templateDir.listFiles()?.forEach { it.delete() }
    }
}

/**
 * Note: brief English comment.
 */
data class ApkConfig(
    val appName: String,
    val packageName: String,
    val targetUrl: String,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val iconPath: String? = null,
    
    // Note: brief English comment.
    val activationEnabled: Boolean = false,
    val activationCodes: List<String> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogTitle: String = "",
    val activationDialogSubtitle: String = "",
    val activationDialogInputLabel: String = "",
    val activationDialogButtonText: String = "",
    
    // Note: brief English comment.
    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    
    // Announcement
    val announcementEnabled: Boolean = false,
    val announcementTitle: String = "",
    val announcementContent: String = "",
    val announcementLink: String = "",
    val announcementLinkText: String = "",
    val announcementTemplate: String = "XIAOHONGSHU",
    val announcementShowEmoji: Boolean = true,
    val announcementAnimationEnabled: Boolean = true,
    val announcementShowOnce: Boolean = true,
    val announcementRequireConfirmation: Boolean = false,
    val announcementAllowNeverShow: Boolean = false,
    
    // Note: brief English comment.
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val zoomEnabled: Boolean = true,
    val desktopMode: Boolean = false,
    val userAgent: String? = null,
    val userAgentMode: String = "DEFAULT", // User-Agent 模式: DEFAULT, CHROME_MOBILE, CHROME_DESKTOP, SAFARI_MOBILE, SAFARI_DESKTOP, FIREFOX_MOBILE, FIREFOX_DESKTOP, EDGE_MOBILE, EDGE_DESKTOP, CUSTOM
    val customUserAgent: String? = null, // Custom User-Agent（仅 CUSTOM 模式使用）
    val hideToolbar: Boolean = false,
    val hideBrowserToolbar: Boolean = false,
    val showStatusBarInFullscreen: Boolean = false,  // Fullscreen模式下是否显示状态栏
    val showNavigationBarInFullscreen: Boolean = false,  // Fullscreen模式下是否显示导航栏
    val showToolbarInFullscreen: Boolean = false,  // Fullscreen模式下是否显示顶部导航栏
    val landscapeMode: Boolean = false, // [已废弃] 向后兼容
    val orientationMode: String = "PORTRAIT", // 屏幕方向模式: PORTRAIT, LANDSCAPE, REVERSE_PORTRAIT, REVERSE_LANDSCAPE, SENSOR_PORTRAIT, SENSOR_LANDSCAPE, AUTO
    val injectScripts: List<com.webtoapp.data.model.UserScript> = emptyList(), // User注入脚本
    
    // Note: brief English comment.
    val statusBarColorMode: String = "THEME", // THEME, TRANSPARENT, CUSTOM
    val statusBarColor: String? = null, // Custom状态栏颜色
    val statusBarDarkIcons: Boolean? = null, // Status bar图标颜色
    val statusBarBackgroundType: String = "COLOR", // COLOR, IMAGE
    val statusBarBackgroundImage: String? = null, // Cropped image path
    val statusBarBackgroundAlpha: Float = 1.0f, // Alpha 0.0-1.0
    val statusBarHeightDp: Int = 0, // Custom高度dp（0=系统默认）
    // Note: brief English comment.
    val statusBarColorModeDark: String = "THEME",
    val statusBarColorDark: String? = null,
    val statusBarDarkIconsDark: Boolean? = null,
    val statusBarBackgroundTypeDark: String = "COLOR",
    val statusBarBackgroundImageDark: String? = null,
    val statusBarBackgroundAlphaDark: Float = 1.0f,
    val longPressMenuEnabled: Boolean = true, // Yes否启用长按菜单
    val longPressMenuStyle: String = "FULL", // DISABLED, SIMPLE, FULL
    val adBlockToggleEnabled: Boolean = false, // Allow用户在运行时切换广告拦截开关
    val popupBlockerEnabled: Boolean = true, // 启用弹窗拦截器
    val popupBlockerToggleEnabled: Boolean = false, // Allow用户在运行时切换弹窗拦截开关
    val openExternalLinks: Boolean = false, // External链接是否在浏览器打开
    
    // Note: brief English comment.
    val initialScale: Int = 0, // Initial scale (0-200, 0=自动)
    val viewportMode: String = "DEFAULT", // DEFAULT, FIT_SCREEN, DESKTOP
    val newWindowBehavior: String = "SAME_WINDOW", // SAME_WINDOW, EXTERNAL_BROWSER, POPUP_WINDOW, BLOCK
    val enablePaymentSchemes: Boolean = true, // Enable支付宝/微信等支付 scheme 拦截
    val enableShareBridge: Boolean = true, // Enable navigator.share 桥接
    val enableZoomPolyfill: Boolean = true, // Enable CSS zoom polyfill
    val enableCrossOriginIsolation: Boolean = false, // 启用跨域隔离（SharedArrayBuffer/FFmpeg.wasm 支持）
    val disableShields: Boolean = false, // 禁用 BrowserShields（游戏/需要完整网络功能的应用）
    val keepScreenOn: Boolean = false, // [向后兼容] 保持屏幕常亮
    val screenAwakeMode: String = "OFF", // 屏幕常亮模式: OFF, ALWAYS, TIMED
    val screenAwakeTimeoutMinutes: Int = 30, // 定时常亮时长（分钟）
    val screenBrightness: Int = -1, // 屏幕亮度：-1=跟随系统, 0-100=自定义百分比
    val keyboardAdjustMode: String = "RESIZE", // 键盘调整模式: RESIZE, NOTHING
    val showFloatingBackButton: Boolean = true, // 全屏模式下是否显示悬浮返回按钮
    val swipeRefreshEnabled: Boolean = true, // 下拉刷新
    val fullscreenEnabled: Boolean = true, // 视频全屏
    val performanceOptimization: Boolean = false, // 运行时性能优化脚本
    val pwaOfflineEnabled: Boolean = false, // PWA Service Worker 离线缓存
    val pwaOfflineStrategy: String = "NETWORK_FIRST", // CACHE_FIRST, NETWORK_FIRST, STALE_WHILE_REVALIDATE
    
    // Note: brief English comment.
    val errorPageMode: String = "BUILTIN_STYLE", // DEFAULT, BUILTIN_STYLE, CUSTOM_HTML, CUSTOM_MEDIA
    val errorPageBuiltInStyle: String = "MATERIAL", // MATERIAL, SATELLITE, OCEAN, FOREST, MINIMAL, NEON
    val errorPageShowMiniGame: Boolean = false,
    val errorPageMiniGameType: String = "RANDOM",
    val errorPageAutoRetrySeconds: Int = 15,
    
    // Note: brief English comment.
    val floatingWindowEnabled: Boolean = false,
    val floatingWindowSizePercent: Int = 80,     // [向后兼容] 窗口大小百分比 50-100
    val floatingWindowWidthPercent: Int = 80,    // 独立宽度百分比 30-100
    val floatingWindowHeightPercent: Int = 80,   // 独立高度百分比 30-100
    val floatingWindowLockAspectRatio: Boolean = true, // 锁定宽高比
    val floatingWindowOpacity: Int = 100,         // 透明度百分比 30-100
    val floatingWindowCornerRadius: Int = 16,     // 圆角半径 dp (0-32)
    val floatingWindowBorderStyle: String = "SUBTLE", // NONE, SUBTLE, GLOW, ACCENT
    val floatingWindowShowTitleBar: Boolean = true,
    val floatingWindowAutoHideTitleBar: Boolean = false,
    val floatingWindowStartMinimized: Boolean = false,
    val floatingWindowRememberPosition: Boolean = true,
    val floatingWindowEdgeSnapping: Boolean = true,
    val floatingWindowShowResizeHandle: Boolean = true,
    val floatingWindowLockPosition: Boolean = false,
    
    // Note: brief English comment.
    val splashEnabled: Boolean = false,
    val splashType: String = "IMAGE",      // "IMAGE" or "VIDEO"
    val splashDuration: Int = 3,           // Show时长（秒）
    val splashClickToSkip: Boolean = true, // Yes否允许点击跳过
    val splashVideoStartMs: Long = 0,      // Video裁剪起始（毫秒）
    val splashVideoEndMs: Long = 5000,     // Video裁剪结束（毫秒）
    val splashLandscape: Boolean = false,  // Yes否横屏显示
    val splashFillScreen: Boolean = true,  // Yes否自动放大铺满屏幕
    val splashEnableAudio: Boolean = false, // Yes否启用视频音频
    
    // Note: brief English comment.
    val appType: String = "WEB",           // "WEB", "IMAGE", "VIDEO", "HTML"
    val mediaEnableAudio: Boolean = true,  // Video是否启用音频
    val mediaLoop: Boolean = true,         // Yes否循环播放
    val mediaAutoPlay: Boolean = true,     // Yes否自动播放
    val mediaFillScreen: Boolean = true,   // Yes否铺满屏幕
    val mediaLandscape: Boolean = false,   // Yes否横屏显示
    val mediaKeepScreenOn: Boolean = true, // 保持屏幕常亮
    
    // Note: brief English comment.
    val htmlEntryFile: String = "index.html",  // HTML入口文件名
    val htmlEnableJavaScript: Boolean = true,  // Yes否启用JavaScript
    val htmlEnableLocalStorage: Boolean = true, // Yes否启用本地存储
    val htmlLandscapeMode: Boolean = false,    // HTML应用横屏模式
    
    // Note: brief English comment.
    val galleryItems: List<GalleryShellItemConfig> = emptyList(),
    val galleryPlayMode: String = "SEQUENTIAL",
    val galleryImageInterval: Int = 3,
    val galleryLoop: Boolean = true,
    val galleryAutoPlay: Boolean = false,
    val galleryBackgroundColor: String = "#000000",
    val galleryShowThumbnailBar: Boolean = true,
    val galleryShowMediaInfo: Boolean = true,
    val galleryOrientation: String = "PORTRAIT",
    val galleryEnableAudio: Boolean = true,
    val galleryVideoAutoNext: Boolean = true,
    
    // Note: brief English comment.
    val bgmEnabled: Boolean = false,       // Yes否启用背景音乐
    val bgmPlaylist: List<BgmShellItem> = emptyList(), // Play列表
    val bgmPlayMode: String = "LOOP",      // Play模式: LOOP, SEQUENTIAL, SHUFFLE
    val bgmVolume: Float = 0.5f,           // Volume (0.0-1.0)
    val bgmAutoPlay: Boolean = true,       // Yes否自动播放
    val bgmShowLyrics: Boolean = true,     // Yes否显示歌词
    val bgmLrcTheme: LrcShellTheme? = null, // Lyrics主题
    
    // Note: brief English comment.
    val themeType: String = "AURORA",      // Theme类型
    val darkMode: String = "SYSTEM",       // Dark mode: SYSTEM, LIGHT, DARK
    
    // Note: brief English comment.
    val translateEnabled: Boolean = false,        // Yes否启用自动翻译
    val translateTargetLanguage: String = "zh-CN", // 目标语言: zh-CN, en, ja, ar
    val translateShowButton: Boolean = true,      // Yes否显示翻译按钮
    
    // Note: brief English comment.
    val extensionModuleIds: List<String> = emptyList(), // Enable的扩展模块ID列表
    val embeddedExtensionModules: List<EmbeddedExtensionModule> = emptyList(), // 嵌入的扩展模块完整数据
    val extensionFabIcon: String = "", // 扩展模块悬浮按钮自定义图标
    
    // Note: brief English comment.
    val autoStartEnabled: Boolean = false,
    val bootStartEnabled: Boolean = false,
    val scheduledStartEnabled: Boolean = false,
    val scheduledTime: String = "08:00",
    val scheduledDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),

    // Note: brief English comment.
    val forcedRunConfig: ForcedRunConfig? = null,
    
    // Note: brief English comment.
    val isolationEnabled: Boolean = false,
    val isolationConfig: com.webtoapp.core.isolation.IsolationConfig? = null,
    
    // Note: brief English comment.
    val backgroundRunEnabled: Boolean = false,
    val backgroundRunConfig: BackgroundRunConfig? = null,
    
    // Note: brief English comment.
    val blackTechConfig: com.webtoapp.core.blacktech.BlackTechConfig? = null,
    
    // Note: brief English comment.
    val disguiseConfig: com.webtoapp.core.disguise.DisguiseConfig? = null,
    
    // Note: brief English comment.
    val language: String = "ENGLISH",  // CHINESE, ENGLISH, ARABIC
    
    // Note: brief English comment.
    val engineType: String = "SYSTEM_WEBVIEW",  // SYSTEM_WEBVIEW, GECKOVIEW
    
    // Note: brief English comment.
    val deepLinkEnabled: Boolean = false,       // 是否启用链接打开
    val deepLinkHosts: List<String> = emptyList(), // 匹配的域名列表
    
    // Note: brief English comment.
    val wordpressSiteTitle: String = "",       // 站点标题
    val wordpressPhpPort: Int = 0,             // PHP 服务器端口（0=自动分配）
    val wordpressLandscapeMode: Boolean = false, // 横屏模式
    
    // Note: brief English comment.
    val nodejsMode: String = "STATIC",         // STATIC, BACKEND, FULLSTACK
    val nodejsPort: Int = 0,                   // Node.js 服务器端口（0=自动分配）
    val nodejsEntryFile: String = "",          // 入口文件（backend 模式需要）
    val nodejsEnvVars: Map<String, String> = emptyMap(), // 环境变量
    val nodejsLandscapeMode: Boolean = false,  // 横屏模式
    
    // Note: brief English comment.
    val phpAppFramework: String = "",           // 框架名称
    val phpAppDocumentRoot: String = "",        // Web 根目录
    val phpAppEntryFile: String = "index.php",  // 入口文件
    val phpAppPort: Int = 0,                    // PHP 端口
    val phpAppEnvVars: Map<String, String> = emptyMap(),
    val phpAppLandscapeMode: Boolean = false,
    
    // Note: brief English comment.
    val pythonAppFramework: String = "",
    val pythonAppEntryFile: String = "app.py",
    val pythonAppEntryModule: String = "",
    val pythonAppServerType: String = "builtin",
    val pythonAppPort: Int = 0,
    val pythonAppEnvVars: Map<String, String> = emptyMap(),
    val pythonAppLandscapeMode: Boolean = false,
    
    // Note: brief English comment.
    val goAppFramework: String = "",
    val goAppBinaryName: String = "",
    val goAppPort: Int = 0,
    val goAppStaticDir: String = "",
    val goAppEnvVars: Map<String, String> = emptyMap(),
    val goAppLandscapeMode: Boolean = false,
    
    // Note: brief English comment.
    val multiWebSites: List<com.webtoapp.core.shell.MultiWebSiteShellConfig> = emptyList(),
    val multiWebDisplayMode: String = "TABS",
    val multiWebRefreshInterval: Int = 30,
    val multiWebShowSiteIcons: Boolean = true,
    val multiWebLandscapeMode: Boolean = false,
    
    // Note: brief English comment.
    val cloudSdkConfig: com.webtoapp.core.shell.CloudSdkConfig = com.webtoapp.core.shell.CloudSdkConfig()
)

/**
 * Note: brief English comment.
 */
data class BackgroundRunConfig(
    val notificationTitle: String = "",
    val notificationContent: String = "",
    val showNotification: Boolean = true,
    val keepCpuAwake: Boolean = true
)

/**
 * Note: brief English comment.
 */
data class GalleryShellItemConfig(
    val id: String,
    val assetPath: String,  // assets/gallery/item_0.{png|mp4}
    val type: String,       // IMAGE or VIDEO
    val name: String,
    val duration: Long = 0,
    val thumbnailPath: String? = null  // assets/gallery/thumb_0.jpg
)

/**
 * Note: brief English comment.
 * Note: brief English comment.
 */
data class EmbeddedExtensionModule(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String = "package",
    val category: String = "OTHER",
    val code: String = "",
    val cssCode: String = "",
    val runAt: String = "DOCUMENT_END",
    val urlMatches: List<EmbeddedUrlMatchRule> = emptyList(),
    val configValues: Map<String, String> = emptyMap(),
    val enabled: Boolean = true
)

/**
 * Note: brief English comment.
 */
data class EmbeddedUrlMatchRule(
    val pattern: String,
    val isRegex: Boolean = false,
    val exclude: Boolean = false
)
