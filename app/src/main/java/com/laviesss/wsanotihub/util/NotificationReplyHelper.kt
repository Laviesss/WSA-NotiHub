package com.laviesss.wsanotihub.util

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.util.Log

object NotificationReplyHelper {
    fun sendReply(context: Context, sbn: StatusBarNotification, replyText: String): Boolean {
        val actions = sbn.notification.actions ?: return false
        val action = actions.find { it.remoteInputs != null && it.remoteInputs.isNotEmpty() } ?: return false
        val remoteInput = action.remoteInputs[0] ?: return false

        val results = Bundle().apply {
            putCharSequence(remoteInput.resultKey, replyText)
        }

        val intent = Intent().apply {
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, results)

        try {
            action.actionIntent.send(context, 0, intent)
            return true
        } catch (e: Exception) {
            Log.e("ReplyHelper", "Error sending reply", e)
            return false
        }
    }
}
