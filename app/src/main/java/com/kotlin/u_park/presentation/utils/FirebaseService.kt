package com.kotlin.u_park.presentation.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.SessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseNotificationUtils {

    private const val CHANNEL_ID = "upark_channel"
    private const val CHANNEL_NAME = "U-Park Notifications"
    private const val CHANNEL_DESCRIPTION = "Notificaciones de reservas y estado de parqueo"

    /** 🔥 REGISTRAR TOKEN (con UPSERT REAL) */
    fun registerFCMToken(
        context: Context,
        supabase: SupabaseClient,
        session: SessionManager
    ) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM", "🔥 Token obtenido: $token")

            CoroutineScope(Dispatchers.IO).launch {
                val user = session.getUser() ?: return@launch

                try {
                    supabase.postgrest.from("device_tokens").upsert(
                        mapOf(
                            "user_id" to user.id,
                            "token" to token
                        )
                    )
                    Log.d("FCM", "🔥 Token registrado/actualizado en Supabase.")
                } catch (e: Exception) {
                    Log.e("FCM", "❌ Error guardando token: ${e.message}")
                }
            }
        }
    }

    /** 🔥 Crear canal con configuración avanzada */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }

            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    /** 🔥 Mostrar notificación con estilo mejorado */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        intent: Intent? = null
    ) {
        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.up) // Usa tu up.png aquí
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(Color.parseColor("#2196F3")) // Color azul para el icono
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .apply {
                pendingIntent?.let { setContentIntent(it) }
            }

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    /** 🔥 Mostrar notificación con acciones */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotificationWithActions(
        context: Context,
        title: String,
        message: String,
        actionIntent: PendingIntent? = null,
        actionTitle: String = "Ver"
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.up)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(Color.parseColor("#2196F3"))
            .apply {
                actionIntent?.let {
                    addAction(R.drawable.up, actionTitle, it)
                }
            }

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}