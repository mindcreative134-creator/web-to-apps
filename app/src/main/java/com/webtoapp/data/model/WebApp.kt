package com.webtoapp.data.model

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.webtoapp.data.converter.Converters
import com.webtoapp.data.model.webapp.config.AdConfig
import com.webtoapp.data.model.webapp.config.Announcement
import com.webtoapp.data.model.webapp.config.ApkExportConfig
import com.webtoapp.data.model.webapp.config.AutoStartConfig
import com.webtoapp.data.model.webapp.config.BgmConfig
import com.webtoapp.data.model.webapp.config.GalleryConfig
import com.webtoapp.data.model.webapp.config.HtmlConfig
import com.webtoapp.data.model.webapp.config.MediaConfig
import com.webtoapp.data.model.webapp.config.SplashConfig
import com.webtoapp.data.model.webapp.config.TranslateConfig
import com.webtoapp.data.model.webapp.config.WebViewConfig
import com.webtoapp.data.model.webapp.config.NodeJsConfig
import com.webtoapp.data.model.webapp.config.WordPressConfig
import com.webtoapp.data.model.webapp.config.PhpAppConfig
import com.webtoapp.data.model.webapp.config.PythonAppConfig
import com.webtoapp.data.model.webapp.config.GoAppConfig
import com.webtoapp.data.model.webapp.config.MultiWebConfig
import com.webtoapp.data.model.webapp.config.ActivationDialogConfig

@Entity(
    tableName = "web_apps",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["categoryId"]),
        Index(value = ["isActivated"])
    ]
)
@TypeConverters(Converters::class)
@Stable
data class WebApp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val iconPath: String? = null,
    val packageName: String? = null,
    val appType: AppType = AppType.WEB,
    val mediaConfig: MediaConfig? = null,
    val galleryConfig: GalleryConfig? = null,
    val htmlConfig: HtmlConfig? = null,
    val wordpressConfig: WordPressConfig? = null,
    val nodejsConfig: NodeJsConfig? = null,
    val phpAppConfig: PhpAppConfig? = null,
    val pythonAppConfig: PythonAppConfig? = null,
    val goAppConfig: GoAppConfig? = null,
    val multiWebConfig: MultiWebConfig? = null,
    val activationEnabled: Boolean = false,
    val activationCodes: List<String> = emptyList(),
    val activationCodeList: List<com.webtoapp.core.activation.ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val isActivated: Boolean = false,
    val adsEnabled: Boolean = false,
    val adConfig: AdConfig? = null,
    val announcementEnabled: Boolean = false,
    val announcement: Announcement? = null,
    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    val webViewConfig: WebViewConfig = WebViewConfig(),
    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig? = null,
    val bgmEnabled: Boolean = false,
    val bgmConfig: BgmConfig? = null,
    val apkExportConfig: ApkExportConfig? = null,
    val themeType: String = "AURORA",
    val translateEnabled: Boolean = false,
    val translateConfig: TranslateConfig? = null,
    val extensionEnabled: Boolean = false,
    val extensionModuleIds: List<String> = emptyList(),
    val extensionFabIcon: String? = null,
    val autoStartConfig: AutoStartConfig? = null,
    val forcedRunConfig: com.webtoapp.core.forcedrun.ForcedRunConfig? = null,
    val blackTechConfig: com.webtoapp.core.blacktech.BlackTechConfig? = null,
    val disguiseConfig: com.webtoapp.core.disguise.DisguiseConfig? = null,
    val browserDisguiseConfig: com.webtoapp.core.disguise.BrowserDisguiseConfig? = null,
    val deviceDisguiseConfig: com.webtoapp.core.disguise.DeviceDisguiseConfig? = null,
    val activationDialogConfig: ActivationDialogConfig? = null,
    val categoryId: Long? = null,
    val cloudConfig: CloudAppConfig? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)




