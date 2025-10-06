package com.zigzura.droplets.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zigzura.droplets.MainActivity
import com.zigzura.droplets.R
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
        private const val CHANNEL_ID = "reminders"
        private const val CHANNEL_NAME = "Reminders"
        private const val WAKE_LOCK_TAG = "Droplets:ReminderWakelock"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "ReminderReceiver triggered")

        var wakeLock: PowerManager.WakeLock? = null

        try {
            // 1. Acquire wake lock temporarily
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            )
            wakeLock.acquire(30000) // Hold for max 30 seconds

            Log.d(TAG, "Wake lock acquired")

            // 2. Extract data from intent
            val reminderId = intent.getStringExtra("reminderId")
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")
            val appId = intent.getStringExtra("appId")

            if (reminderId == null || appId == null) {
                Log.e(TAG, "Missing required data in intent")
                return
            }

            Log.d(TAG, "Processing reminder: $reminderId for app: $appId")

            // 3. Create notification channel (required for Android 8.0+)
            createNotificationChannel(context)

            // 4. Show notification
            showNotification(context, title, message, appId, reminderId)

            // 5. Clean up reminder from reminders.json
            cleanupReminder(context, appId, reminderId)

            Log.d(TAG, "Reminder processed successfully: $reminderId")

        } catch (e: Exception) {
            Log.e(TAG, "Error processing reminder", e)
        } finally {
            // 6. Always release wake lock
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
        }
    }

    /**
     * Create notification channel for reminders (Android 8.0+)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for app reminders"
                    enableVibration(true)
                    setShowBadge(true)
                }

                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $CHANNEL_ID")
            }
        }
    }

    /**
     * Show notification for the reminder
     */
    private fun showNotification(context: Context, title: String?, message: String?, appId: String, reminderId: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create intent to open the app when notification is tapped
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("appId", appId)
                putExtra("reminderId", reminderId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                reminderId.hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title ?: "Reminder")
                .setContentText(message ?: "You have a reminder")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)

            // Use unique notification ID for each reminder
            val notificationId = "${appId}_$reminderId".hashCode()

            notificationManager.notify(notificationId, builder.build())

            Log.d(TAG, "Notification shown: $title (ID: $notificationId)")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    /**
     * Clean up the fired reminder from reminders.json
     */
    private fun cleanupReminder(context: Context, appId: String, reminderId: String) {
        try {
            // Get app directory
            val appsDir = File(context.filesDir, "apps")
            val appDir = File(appsDir, appId)
            val remindersFile = File(appDir, "reminders.json")

            if (!remindersFile.exists()) {
                Log.d(TAG, "Reminders file not found for cleanup: $appId")
                return
            }

            // Read existing reminders
            val fis = FileInputStream(remindersFile)
            val data = ByteArray(remindersFile.length().toInt())
            fis.read(data)
            fis.close()

            val jsonString = String(data, StandardCharsets.UTF_8)
            val reminders = JSONArray(jsonString)

            // Remove the fired reminder
            var found = false
            for (i in 0 until reminders.length()) {
                val reminder = reminders.getJSONObject(i)
                if (reminderId == reminder.optString("id")) {
                    reminders.remove(i)
                    found = true
                    Log.d(TAG, "Removed fired reminder: $reminderId")
                    break
                }
            }

            if (found) {
                // Write updated reminders back to file
                val fos = FileOutputStream(remindersFile)
                fos.write(reminders.toString().toByteArray(StandardCharsets.UTF_8))
                fos.close()

                Log.d(TAG, "Reminders file updated after cleanup")
            } else {
                Log.w(TAG, "Reminder not found for cleanup: $reminderId")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up reminder: $reminderId", e)
        }
    }
}