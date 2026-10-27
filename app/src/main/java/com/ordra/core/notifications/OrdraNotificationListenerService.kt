package com.ordra.core.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.service.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Optional marketplace/courier/rider notification detection. Ordra works fully
 * without this permission; unrelated notifications are ignored and never stored.
 */
@AndroidEntryPoint
class OrdraNotificationListenerService : NotificationListenerService() {

    @Inject lateinit var detectionRepository: DetectionRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notifier: OrdraNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val marketplacePkgs = setOf(
        "com.shopee.ph", "com.lazada.android", "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill", "com.garenait.epay", "sg.bigo.live",
    )
    private val courierPkgs = setOf(
        "ph.com.spx.express", "com.jtexpress.ph", "com.ninjavan",
        "com.flashexpress", "ph.com.lbc", "com.jrs.express",
    )
    private val messagingPkgs = setOf(
        "com.facebook.orca", "com.facebook.mlite", "com.whatsapp",
        "org.telegram.messenger", "com.viber.voip",
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val combined = "$title $text".trim()
        if (combined.isBlank()) return

        scope.launch {
            val s = settingsRepository.settings.first()
            val source = when {
                pkg in marketplacePkgs && s.detectionMarketplace ->
                    TrackingStatusSource.MARKETPLACE_NOTIFICATION
                pkg in courierPkgs && s.detectionCourier ->
                    TrackingStatusSource.MARKETPLACE_NOTIFICATION
                pkg in messagingPkgs && s.detectionRider ->
                    TrackingStatusSource.MARKETPLACE_NOTIFICATION
                else -> return@launch
            }
            val signals = NotificationParser.parse(combined)
            val suggestion = detectionRepository.ingest(
                signals = signals,
                source = source,
                notificationPackage = pkg,
                summary = combined,
                autoApplyHigh = s.autoApplyHighConfidence,
            )
            if (suggestion != null) {
                notifier.post(
                    notificationId = suggestion.id.hashCode(),
                    channel = NotificationChannels.PARCEL_STATUS,
                    title = "Possible parcel update",
                    body = "Tap Ordra to review: ${suggestion.status.name.lowercase().replace('_', ' ')}",
                )
            }
        }
    }
}
