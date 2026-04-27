package com.webtoapp.ui

import android.Manifest
import android.content.Intent
import com.webtoapp.core.auth.GoogleSignInHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.view.WindowCompat
import com.webtoapp.core.i18n.LanguageManager
import com.webtoapp.core.i18n.AppStringsProvider
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellModeManager
import com.webtoapp.ui.components.FirstLaunchLanguageScreen
import com.webtoapp.ui.navigation.AppNavigation
import com.webtoapp.ui.shell.ShellActivity
import com.webtoapp.ui.theme.CircularRevealOverlay
import com.webtoapp.ui.theme.LocalThemeRevealState
import com.webtoapp.ui.theme.WebToAppTheme
import com.webtoapp.ui.theme.rememberThemeRevealState
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

/**
 * Activity -
 */
class MainActivity : ComponentActivity() {

    private val shellModeManager: ShellModeManager by inject()

    // Whether shell check is complete
    private var shellCheckDone by mutableStateOf(false)
    // true = show main UI, false = redirecting to ShellActivity
    private var showMainUI by mutableStateOf(false)

    private var showLanguageSelection by mutableStateOf(true)
    private var showShortcutPermissionDialog by mutableStateOf(false)
    private var shortcutPermissionMessage by mutableStateOf("")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permission，
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.lifecycle("MainActivity", "onCreate", "savedInstanceState=${savedInstanceState != null}")

        // Google OAuth callback
        handleGoogleOAuthIfNeeded(intent)

        // Apply window flags immediately on main thread (no IO needed)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            AppLogger.w("MainActivity", "enableEdgeToEdge failed", e)
        }
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } catch (e: Exception) {
            AppLogger.w("MainActivity", "setDecorFitsSystemWindows failed", e)
        }

        // Set content immediately so the window is NEVER blank
        setContent {
            val themeRevealState = rememberThemeRevealState()

            WebToAppTheme { isDarkTheme ->
                val themeColors = MaterialTheme.colorScheme
                LaunchedEffect(isDarkTheme, themeColors.background) {
                    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme
                }

                CompositionLocalProvider(
                    LocalThemeRevealState provides themeRevealState
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            // Only render main UI after shell check is done
                            if (shellCheckDone && showMainUI) {
                                val languageManager: LanguageManager = koinInject()
                                val hasSelectedLanguage by languageManager.hasSelectedLanguageFlow.collectAsState(initial = true)
                                if (!hasSelectedLanguage && showLanguageSelection) {
                                    FirstLaunchLanguageScreen(
                                        onLanguageSelected = { showLanguageSelection = false }
                                    )
                                } else {
                                    AppNavigation()
                                }
                            }
                            // Else: transparent surface while shell check runs (instant, <100ms)
                        }
                        CircularRevealOverlay(revealState = themeRevealState)
                    }
                }

                if (showShortcutPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showShortcutPermissionDialog = false },
                        title = { Text(AppStringsProvider.current().shortcutPermissionTitle) },
                        text = { Text(shortcutPermissionMessage) },
                        confirmButton = {
                            TextButton(onClick = {
                                showShortcutPermissionDialog = false
                                openAppSettings()
                            }) {
                                Text(AppStringsProvider.current().shortcutPermissionGoToSettings)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showShortcutPermissionDialog = false }) {
                                Text(AppStringsProvider.current().shortcutPermissionLater)
                            }
                        }
                    )
                }
            }
        }

        // Now check shell mode in the background
        lifecycleScope.launch {
            val isShell = try {
                withContext(Dispatchers.IO) { shellModeManager.isShellMode() }
            } catch (e: Exception) {
                AppLogger.e("MainActivity", "Shell mode check failed", e)
                false
            } catch (e: Throwable) {
                AppLogger.e("MainActivity", "Shell mode check critical error", e)
                false
            }

            if (isShell) {
                AppLogger.i("MainActivity", "Entering shell mode, redirecting to ShellActivity")
                try {
                    startActivity(Intent(this@MainActivity, ShellActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    AppLogger.e("MainActivity", "Failed to start ShellActivity", e)
                    // Fall through to normal mode if redirect fails
                    requestNecessaryPermissions()
                    checkShortcutPermission()
                    showMainUI = true
                    shellCheckDone = true
                }
                return@launch
            }

            AppLogger.i("MainActivity", "Normal mode, showing main UI")
            requestNecessaryPermissions()
            checkShortcutPermission()
            showMainUI = true
            shellCheckDone = true
        }
    }

    /**
     * Comment
     */
    private fun checkShortcutPermission() {
        // Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                // Get
                shortcutPermissionMessage = buildShortcutPermissionMessage()
                showShortcutPermissionDialog = true
            }
        }
    }

    /**
     * Comment
     */
    private fun buildShortcutPermissionMessage(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                AppStringsProvider.current().shortcutPermissionXiaomi
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                AppStringsProvider.current().shortcutPermissionHuawei
            }
            manufacturer.contains("oppo") -> {
                AppStringsProvider.current().shortcutPermissionOppo
            }
            manufacturer.contains("vivo") -> {
                AppStringsProvider.current().shortcutPermissionVivo
            }
            manufacturer.contains("meizu") -> {
                AppStringsProvider.current().shortcutPermissionMeizu
            }
            manufacturer.contains("samsung") -> {
                AppStringsProvider.current().shortcutPermissionSamsung
            }
            else -> {
                AppStringsProvider.current().shortcutPermissionGeneric
            }
        }
    }

    /**
     * Comment
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // ，
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e2: Exception) {
                // Comment
            }
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val needRequest = permissions.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }

        if (needRequest.isNotEmpty()) {
            AppLogger.d("MainActivity", "Requesting permissions: ${needRequest.joinToString()}")
            permissionLauncher.launch(needRequest.toTypedArray())
        }
    }
    
    override fun onStart() {
        super.onStart()
        AppLogger.lifecycle("MainActivity", "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        AppLogger.lifecycle("MainActivity", "onResume")
    }
    
    override fun onPause() {
        AppLogger.lifecycle("MainActivity", "onPause")
        super.onPause()
    }
    
    override fun onStop() {
        AppLogger.lifecycle("MainActivity", "onStop")
        super.onStop()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AppLogger.lifecycle("MainActivity", "onNewIntent")
        // Google OAuth （）
        handleGoogleOAuthIfNeeded(intent)
    }
    
    /**
     * Google OAuth
     */
    private fun handleGoogleOAuthIfNeeded(intent: Intent?) {
        if (GoogleSignInHelper.isOAuthCallback(intent)) {
            val uri = intent?.data ?: return
            AppLogger.i("MainActivity", "Handling Google OAuth callback: ${uri.scheme}://${uri.host}")
            lifecycleScope.launch(Dispatchers.Main) {
                GoogleSignInHelper.handleOAuthCallback(uri)
            }
        }
    }
    
    override fun onDestroy() {
        AppLogger.lifecycle("MainActivity", "onDestroy")
        super.onDestroy()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        AppLogger.lifecycle("MainActivity", "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }
}





