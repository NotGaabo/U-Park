package com.kotlin.u_park.presentation.services

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.presentation.utils.FirebaseNotificationUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UparkFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🔄 Nuevo token generado por FCM: $token")

        val session = SessionManager.getInstance(applicationContext, com.kotlin.u_park.data.remote.supabase)

        CoroutineScope(Dispatchers.IO).launch {
            val user = session.getUser()
            if (user != null) {
                try {
                    com.kotlin.u_park.data.remote.supabase.postgrest.from("device_tokens").upsert(
                        mapOf(
                            "user_id" to user.id,
                            "token" to token
                        )
                    )
                    Log.d("FCM", "🔥 Token actualizado desde onNewToken()")
                } catch (e: Exception) {
                    Log.e("FCM", "❌ Error actualizando token: ${e.message}")
                }
            }
        }
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: "U-Park"
        val body  = message.data["body"]  ?: message.notification?.body  ?: ""

        FirebaseNotificationUtils.showNotification(this, title, body)
    }
}
