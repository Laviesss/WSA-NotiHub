package com.laviesss.wsanotihub.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.laviesss.wsanotihub.data.NotiHubDatabase
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking

class ApiServerService : Service() {

    private var server: NotificationServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        server = NotificationServer(8765, NotiHubDatabase.getDatabase(this))
        try {
            server?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundService() {
        val channelId = "api_server_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "API Server", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("WSA NotiHub API Bridge")
            .setContentText("Listening on port 8765")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1002, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    private class NotificationServer(port: Int, val database: NotiHubDatabase) : NanoHTTPD(port) {
        private val gson = Gson()

        override fun serve(session: IHTTPSession): Response {
            return if (session.uri == "/notifications" && session.method == Method.GET) {
                val notifications = runBlocking {
                    database.notificationDao().getRecentActive()
                }
                val json = gson.toJson(notifications)
                newFixedLengthResponse(Response.Status.OK, "application/json", json)
            } else {
                newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
            }
        }
    }
}
