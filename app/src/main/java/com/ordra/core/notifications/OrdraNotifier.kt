package com.ordra.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

object NotificationChannels {
    const val DELIVERY_REMINDERS = "delivery_reminders"
    const val PARCEL_STATUS = "parcel_status"
    const val DAILY_SUMMARY = "daily_summary"
}

@Singleton
class OrdraNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager get() = context.getSystemService(NotificationManager::class.java)

    init {
        createChannels()
    }

    private fun createChannels() {
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    NotificationChannels.DELIVERY_REMINDERS,
                    "Delivery reminders",
                    NotificationManager.IMPORTANCE_HIGH,
                ),
                NotificationChannel(
                    NotificationChannels.PARCEL_STATUS,
                    "Parcel status updates",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
                NotificationChannel(
                    NotificationChannels.DAILY_SUMMARY,
                    "Daily summary",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            ),
        )
    }

    fun canPost(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun post(notificationId: Int, channel: String, title: String, body: String) {
        if (!canPost()) return
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = launch?.let {
            PendingIntent.getActivity(
                context, notificationId, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(com.ordra.R.drawable.ic_ordra_mark)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        manager.notify(notificationId, notification)
    }
}
