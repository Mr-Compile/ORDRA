package com.ordra.app

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * One-shot intake for share/open intents arriving at MainActivity. The NavHost
 * drains pendingRoute once; SmartImportScreen drains pendingImageUri.
 */
object AppIntake {
    val pendingRoute = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val pendingImageUri = MutableStateFlow<String?>(null)
}
