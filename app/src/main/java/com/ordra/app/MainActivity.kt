package com.ordra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.ordra.app.navigation.OrdraRoutes
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.service.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var detectionRepository: DetectionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            OrdraApp(settingsRepository)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND &&
            intent?.action != Intent.ACTION_SEND_MULTIPLE
        ) return
        when {
            intent.type?.startsWith("image/") == true -> {
                @Suppress("DEPRECATION")
                val uris = if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                    intent.getParcelableArrayListExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                        ?.map { it.toString() }
                } else {
                    intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                        ?.let { listOf(it.toString()) }
                }
                if (!uris.isNullOrEmpty()) {
                    AppIntake.pendingImageUris.value = uris
                    AppIntake.pendingRoute.tryEmit(OrdraRoutes.SMART_IMPORT)
                }
            }
            intent.type == "text/plain" -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
                lifecycleScope.launch(Dispatchers.IO) {
                    detectionRepository.ingest(
                        signals = NotificationParser.parse(text),
                        source = TrackingStatusSource.SHARED_CONTENT,
                        summary = text,
                        autoApplyHigh = settingsRepository.settings.first()
                            .autoApplyHighConfidence,
                    )
                    AppIntake.pendingRoute.tryEmit(OrdraRoutes.DETECTIONS)
                }
            }
        }
    }
}
