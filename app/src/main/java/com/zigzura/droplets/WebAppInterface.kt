package com.zigzura.droplets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID

class WebAppInterface(
    private val context: Context,
    private val currentAppId: String // Locked per app instance - NEVER accept from JavaScript
) {
    companion object {
        private const val TAG = "WebAppInterface"
    }

    init {
        // Ensure app directory exists
        createAppDirectoryIfNeeded()
        Log.d(TAG, "WebAppInterface initialized for appId: $currentAppId")
    }

    // ==================== FILE STORAGE SYSTEM ====================

    /**
     * Creates the app directory structure if it doesn't exist
     */
    private fun createAppDirectoryIfNeeded() {
        val appDir = getAppDirectory()
        if (!appDir.exists()) {
            val created = appDir.mkdirs()
            Log.d(TAG, "App directory created: $created at ${appDir.absolutePath}")
        }
    }

    /**
     * Gets the app-specific directory: files/apps/{currentAppId}/
     */
    private fun getAppDirectory(): File {
        val appsDir = File(context.filesDir, "apps")
        return File(appsDir, currentAppId)
    }

    /**
     * Gets the data.json file for the current app
     */
    private fun getDataFile(): File = File(getAppDirectory(), "data.json")

    /**
     * Gets the reminders.json file for the current app
     */
    private fun getRemindersFile(): File = File(getAppDirectory(), "reminders.json")

    /**
     * Reads JSON data from a file
     */
    private fun readJsonFromFile(file: File): JSONObject {
        if (!file.exists()) {
            return JSONObject()
        }

        return try {
            val fis = FileInputStream(file)
            val data = ByteArray(file.length().toInt())
            fis.read(data)
            fis.close()

            val jsonString = String(data, StandardCharsets.UTF_8)
            JSONObject(jsonString)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON from file: ${file.name}", e)
            JSONObject()
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON from file: ${file.name}", e)
            JSONObject()
        }
    }

    /**
     * Writes JSON data to a file
     */
    private fun writeJsonToFile(file: File, json: JSONObject): Boolean {
        return try {
            val fos = FileOutputStream(file)
            fos.write(json.toString().toByteArray(StandardCharsets.UTF_8))
            fos.close()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error writing JSON to file: ${file.name}", e)
            false
        }
    }

    /**
     * Save data to the app-specific data.json file
     * JavaScript: window.androidInterface.saveData("userPrefs", "{theme: 'dark'}")
     */
    @JavascriptInterface
    fun saveData(key: String?, value: String?) {
        if (key.isNullOrBlank()) {
            Log.w(TAG, "saveData: key is null or empty")
            return
        }

        try {
            val dataFile = getDataFile()
            val data = readJsonFromFile(dataFile)

            data.put(key, value ?: "")

            val success = writeJsonToFile(dataFile, data)
            Log.d(TAG, "saveData: $key = $value (success: $success)")

        } catch (e: JSONException) {
            Log.e(TAG, "Error saving data for key: $key", e)
        }
    }

    /**
     * Load data from the app-specific data.json file
     * JavaScript: let userPrefs = window.androidInterface.loadData("userPrefs")
     */
    @JavascriptInterface
    fun loadData(key: String?): String {
        if (key.isNullOrBlank()) {
            Log.w(TAG, "loadData: key is null or empty")
            return ""
        }

        return try {
            val dataFile = getDataFile()
            val data = readJsonFromFile(dataFile)

            val value = data.optString(key, "")
            Log.d(TAG, "loadData: $key = $value")
            value

        } catch (e: Exception) {
            Log.e(TAG, "Error loading data for key: $key", e)
            ""
        }
    }

    /**
     * Get all data as a JSON string
     * JavaScript: let allData = JSON.parse(window.androidInterface.getAllData())
     */
    @JavascriptInterface
    fun getAllData(): String {
        return try {
            val dataFile = getDataFile()
            val data = readJsonFromFile(dataFile)

            val result = data.toString()
            Log.d(TAG, "getAllData: $result")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Error getting all data", e)
            "{}"
        }
    }

    /**
     * Delete a key from the app-specific data.json file
     * JavaScript: window.androidInterface.deleteData("userPrefs")
     */
    @JavascriptInterface
    fun deleteData(key: String?) {
        if (key.isNullOrBlank()) {
            Log.w(TAG, "deleteData: key is null or empty")
            return
        }

        try {
            val dataFile = getDataFile()
            val data = readJsonFromFile(dataFile)

            if (data.has(key)) {
                data.remove(key)
                val success = writeJsonToFile(dataFile, data)
                Log.d(TAG, "deleteData: $key (success: $success)")
            } else {
                Log.d(TAG, "deleteData: key not found: $key")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting data for key: $key", e)
        }
    }

    // ==================== REMINDER/ALARM SYSTEM ====================

    /**
     * Set a reminder/alarm
     * JavaScript: window.androidInterface.setReminder("morning-coffee", Date.now() + 600000, "Coffee Time", "Time for your morning coffee!")
     */
    @JavascriptInterface
    fun setReminder(reminderId: String?, timeMillis: Long, title: String?, message: String?) {
        if (reminderId.isNullOrBlank()) {
            Log.w(TAG, "setReminder: reminderId is null or empty")
            return
        }

        val reminderTitle = title ?: "Reminder"
        val reminderMessage = message ?: ""

        try {
            // 1. Save reminder info to reminders.json
            saveReminderToFile(reminderId, timeMillis, reminderTitle, reminderMessage)

            // 2. Schedule alarm using AlarmManager
            scheduleAlarm(reminderId, timeMillis, reminderTitle, reminderMessage)

            Log.d(TAG, "setReminder: $reminderId scheduled for $timeMillis")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting reminder: $reminderId", e)
        }
    }

    /**
     * Cancel a reminder/alarm
     * JavaScript: window.androidInterface.cancelReminder("morning-coffee")
     */
    @JavascriptInterface
    fun cancelReminder(reminderId: String?) {
        if (reminderId.isNullOrBlank()) {
            Log.w(TAG, "cancelReminder: reminderId is null or empty")
            return
        }

        try {
            // 1. Cancel alarm via AlarmManager
            cancelAlarm(reminderId)

            // 2. Remove from reminders.json
            removeReminderFromFile(reminderId)

            Log.d(TAG, "cancelReminder: $reminderId cancelled")

        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder: $reminderId", e)
        }
    }

    /**
     * Get all reminders as JSON array
     * JavaScript: let reminders = JSON.parse(window.androidInterface.getReminders())
     */
    @JavascriptInterface
    fun getReminders(): String {
        return try {
            val remindersFile = getRemindersFile()
            if (!remindersFile.exists()) {
                return "[]"
            }

            // Read as JSONArray instead of JSONObject
            val fis = FileInputStream(remindersFile)
            val data = ByteArray(remindersFile.length().toInt())
            fis.read(data)
            fis.close()

            val jsonString = String(data, StandardCharsets.UTF_8)

            // Validate it's a valid JSON array
            val reminders = JSONArray(jsonString)
            val result = reminders.toString()

            Log.d(TAG, "getReminders: $result")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminders", e)
            "[]"
        }
    }

    /**
     * Save reminder to reminders.json file
     */
    private fun saveReminderToFile(reminderId: String, timeMillis: Long, title: String, message: String) {
        try {
            val remindersFile = getRemindersFile()
            val reminders = if (remindersFile.exists()) {
                // Read existing reminders
                val fis = FileInputStream(remindersFile)
                val data = ByteArray(remindersFile.length().toInt())
                fis.read(data)
                fis.close()

                val jsonString = String(data, StandardCharsets.UTF_8)
                JSONArray(jsonString)
            } else {
                JSONArray()
            }

            // Remove existing reminder with same ID
            for (i in 0 until reminders.length()) {
                val reminder = reminders.getJSONObject(i)
                if (reminderId == reminder.optString("id")) {
                    reminders.remove(i)
                    break
                }
            }

            // Add new reminder
            val newReminder = JSONObject().apply {
                put("id", reminderId)
                put("timeMillis", timeMillis)
                put("title", title)
                put("message", message)
                put("appId", currentAppId)
            }

            reminders.put(newReminder)

            // Write back to file
            val fos = FileOutputStream(remindersFile)
            fos.write(reminders.toString().toByteArray(StandardCharsets.UTF_8))
            fos.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving reminder to file: $reminderId", e)
        }
    }

    /**
     * Remove reminder from reminders.json file
     */
    private fun removeReminderFromFile(reminderId: String) {
        try {
            val remindersFile = getRemindersFile()
            if (!remindersFile.exists()) {
                return
            }

            // Read existing reminders
            val fis = FileInputStream(remindersFile)
            val data = ByteArray(remindersFile.length().toInt())
            fis.read(data)
            fis.close()

            val jsonString = String(data, StandardCharsets.UTF_8)
            val reminders = JSONArray(jsonString)

            // Remove reminder with matching ID
            for (i in 0 until reminders.length()) {
                val reminder = reminders.getJSONObject(i)
                if (reminderId == reminder.optString("id")) {
                    reminders.remove(i)
                    break
                }
            }

            // Write back to file
            val fos = FileOutputStream(remindersFile)
            fos.write(reminders.toString().toByteArray(StandardCharsets.UTF_8))
            fos.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error removing reminder from file: $reminderId", e)
        }
    }

    /**
     * Schedule alarm using AlarmManager
     */
    private fun scheduleAlarm(reminderId: String, timeMillis: Long, title: String, message: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("reminderId", reminderId)
                putExtra("title", title)
                putExtra("message", message)
                putExtra("appId", currentAppId)
            }

            // Create unique request code for this reminder
            val requestCode = "${currentAppId}_$reminderId".hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }

            Log.d(TAG, "Alarm scheduled: $reminderId at $timeMillis")

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: $reminderId", e)
        }
    }

    /**
     * Cancel alarm using AlarmManager
     */
    private fun cancelAlarm(reminderId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, ReminderReceiver::class.java)

            // Create same request code as when scheduling
            val requestCode = "${currentAppId}_$reminderId".hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)

            Log.d(TAG, "Alarm cancelled: $reminderId")

        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm: $reminderId", e)
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generate a unique app ID - can be called from JavaScript to get a new ID
     * JavaScript: let newAppId = window.androidInterface.generateAppId()
     */
    @JavascriptInterface
    fun generateAppId(): String = "app_${UUID.randomUUID().toString().replace("-", "").substring(0, 8)}"

    /**
     * Get current app ID - for debugging
     * JavaScript: let currentId = window.androidInterface.getCurrentAppId()
     */
    @JavascriptInterface
    fun getCurrentAppId(): String = currentAppId

    /**
     * Show a toast message - for debugging and user feedback
     * JavaScript: window.androidInterface.showToast("Data saved successfully!")
     */
    @JavascriptInterface
    fun showToast(message: String?) {
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
