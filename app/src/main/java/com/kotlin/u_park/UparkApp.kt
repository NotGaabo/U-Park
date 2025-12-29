package com.kotlin.u_park

import android.app.Application
import com.kotlin.u_park.presentation.utils.FirebaseNotificationUtils

class UparkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Crear canal de notificaciones apenas inicia la app
        FirebaseNotificationUtils.createNotificationChannel(this)
    }
}
