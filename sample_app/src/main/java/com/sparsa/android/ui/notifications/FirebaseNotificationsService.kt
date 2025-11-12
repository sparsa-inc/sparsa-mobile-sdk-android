package com.sparsa.android.ui.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import main.SparsaMobile
import com.sparsa.android.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHandler {
    private val _notification = MutableLiveData<String>()
    val notification: LiveData<String> = _notification

    fun postNotification(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _notification.value = message
        }
    }
}

class FirebaseNotificationsService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        } else if (remoteMessage.notification != null) {
            showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SparsaMobile.updateDeviceToken(token)
    }

    private fun showNotification(title: String?, message: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, "sparsa")
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun handleDataPayload(data: Map<String, String>) {
        try {
            SparsaMobile.handleNotification(data) {
                NotificationHandler.postNotification("delete")
            }
        } catch (e: Exception) {
            Log.e("Notification Failed", e.message ?: e.toString())
        }
    }
}