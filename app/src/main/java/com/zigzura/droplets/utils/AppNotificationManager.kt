package com.zigzura.droplets.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zigzura.droplets.MainActivity
import com.zigzura.droplets.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager @Inject constructor(
    private val permissionManager: PermissionManager
) {

    companion object {
        private const val CHANNEL_ID = "app_generation_channel"
        private const val NOTIFICATION_ID = 1001
    }

    /**
     * Initialize notification channel for app generation notifications
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Generation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app generation completion"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification when app generation completes
     */
    fun showAppGenerationCompleteNotification(
        context: Context,
        appTitle: String? = null
    ) {
        // Check if we have permission to show notifications
        if (!permissionManager.hasNotificationPermission(context)) {
            return
        }

        val title = "App Generated Successfully! 🎉"
        val text = if (appTitle.isNullOrBlank()) {
            "Your new app is ready to use"
        } else {
            "'$appTitle' is ready to use"
        }

        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_my_apps", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You might want to create a specific icon
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where permission was revoked
            android.util.Log.w("AppNotificationManager", "Failed to show notification: permission denied")
        }
    }

    /**
     * Show notification when app generation fails
     */
    fun showAppGenerationFailedNotification(
        context: Context,
        errorMessage: String
    ) {
        // Check if we have permission to show notifications
        if (!permissionManager.hasNotificationPermission(context)) {
            return
        }

        val title = "App Generation Failed"
        val text = "There was an issue generating your app. Tap to try again."

        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_create", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 1, notification)
        } catch (e: SecurityException) {
            android.util.Log.w("AppNotificationManager", "Failed to show notification: permission denied")
        }
    }
}
