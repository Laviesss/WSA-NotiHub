package com.laviesss.wsanotihub.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.laviesss.wsanotihub.data.NotiHubDatabase
import com.laviesss.wsanotihub.data.NotificationEntity
import com.laviesss.wsanotihub.model.ActionType
import com.laviesss.wsanotihub.model.AutomationRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class NotiHubListenerService : NotificationListenerService() {

    private val gson = Gson()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var database: NotiHubDatabase

    override fun onCreate() {
        super.onCreate()
        database = NotiHubDatabase.getDatabase(this)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        serviceScope.launch {
            database.notificationDao().markAsDismissed(sbn.packageName, sbn.postTime)
        }
        activeNotificationsMap.remove(sbn.key)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val appLabel = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val actions = notification.actions?.map { action ->
            mapOf(
                "title" to action.title.toString(),
                "hasRemoteInput" to (action.remoteInputs != null && action.remoteInputs.isNotEmpty())
            )
        } ?: emptyList()

        val entity = NotificationEntity(
            notificationKey = sbn.key,
            appPackageName = sbn.packageName,
            appLabel = appLabel,
            title = title,
            body = body,
            timestamp = sbn.postTime,
            priority = notification.priority,
            hasReplyAction = actions.any { it["hasRemoteInput"] == true },
            serializedActions = gson.toJson(actions)
        )

        serviceScope.launch {
            database.notificationDao().insert(entity)
            applyAutomationRules(sbn, title, body)
        }

        activeNotificationsMap[sbn.key] = sbn
    }

    private fun applyAutomationRules(sbn: StatusBarNotification, title: String, body: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val rulesJson = prefs.getString("automation_rules", "[]")
        val type = object : TypeToken<List<AutomationRule>>() {}.type
        val rules: List<AutomationRule> = gson.fromJson(rulesJson, type)

        rules.filter { it.isEnabled }.forEach { rule ->
            if (rule.triggerPackage == "*" || rule.triggerPackage == sbn.packageName) {
                when (rule.actionType) {
                    ActionType.AUTO_COPY_OTP -> {
                        val otpPattern = Pattern.compile("\\b\\d{4,8}\\b")
                        val matcher = otpPattern.matcher(body)
                        if (matcher.find()) {
                            val otp = matcher.group()
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("OTP", otp)
                            clipboard.setPrimaryClip(clip)
                        }
                    }
                    ActionType.AUTO_DISMISS -> {
                        cancelNotification(sbn.key)
                    }
                    ActionType.OPEN_APP -> {
                        try {
                            sbn.notification.contentIntent?.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val activeNotificationsMap = mutableMapOf<String, StatusBarNotification>()

        fun getActiveSbn(key: String): StatusBarNotification? = activeNotificationsMap[key]
    }
}
