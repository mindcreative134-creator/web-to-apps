package com.webtoapp.core.startup

import com.webtoapp.core.activation.ActivationManager
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.announcement.AnnouncementManager
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellModeManager
import com.webtoapp.core.shell.ShellRuntimeServices

class ShellRuntimeStartup(
    private val shellModeManager: ShellModeManager,
    private val activationManager: ActivationManager,
    private val announcementManager: AnnouncementManager,
    private val adBlocker: AdBlocker,
) {

    fun initialize() {
        ShellRuntimeServices.initialize(
            shellModeManager = shellModeManager,
            activationManager = activationManager,
            announcementManager = announcementManager,
            adBlocker = adBlocker,
        )

        AppLogger.i("WebToAppApplication", "ShellRuntimeServices initialized")
    }

    fun shutdown() {
        ShellRuntimeServices.reset()
    }
}





