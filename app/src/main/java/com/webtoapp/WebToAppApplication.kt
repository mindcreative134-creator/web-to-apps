package com.webtoapp

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.i18n.LanguageManager
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.startup.AppStartupManager
import com.webtoapp.di.appModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Application entry point.
 *
 * Starts Koin and forwards lifecycle events to the startup manager.
 */
class WebToAppApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val startupManager: AppStartupManager by inject()
    private val languageManager: LanguageManager by inject()

    override fun onCreate() {
        super.onCreate()
        
        // Setup global crash handler for better debugging
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Extract stack trace
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()
            
            // Log to logcat
            Log.e("CRASH_FATAL", "Uncaught exception in thread ${thread.name}: \n$stackTrace")
            
            // Try to show CrashActivity
            try {
                val intent = android.content.Intent(this, com.webtoapp.ui.CrashActivity::class.java).apply {
                    putExtra("CRASH_LOG", stackTrace)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(1)
            } catch (e: Exception) {
                oldHandler?.uncaughtException(thread, throwable)
            }
        }

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(this@WebToAppApplication)
                modules(appModules)
            }
            try {
                AppStringsProvider.initialize(AppLanguage.ENGLISH)
            } catch (e: Exception) {
                AppLogger.e("WebToAppApplication", "Language init failed", e)
            }
            appScope.launch {
                try {
                    val lang = languageManager.getCurrentLanguage()
                    AppStringsProvider.syncLanguage(lang)
                } catch (e: Exception) {
                    AppLogger.e("WebToAppApplication", "Language sync failed", e)
                }
            }
            try {
                startupManager.initialize(appScope)
            } catch (e: Throwable) {
                Log.e("WebToAppApplication", "StartupManager initialization critical failure", e)
                AppLogger.e("WebToAppApplication", "StartupManager initialization critical failure", e)
            }
        } else {
            AppLogger.w("WebToAppApplication", "Koin already started, skip duplicate application initialization")
        }
    }

    override fun onTerminate() {
        if (GlobalContext.getOrNull() != null) {
            startupManager.shutdown(appScope)
            GlobalContext.stopKoin()
        }
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppLogger.w("WebToAppApplication", "onLowMemory triggered")
        startupManager.clearCaches()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val levelName = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }
        AppLogger.d("WebToAppApplication", "onTrimMemory: $levelName")

        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            startupManager.clearCaches()
        }
    }
}



