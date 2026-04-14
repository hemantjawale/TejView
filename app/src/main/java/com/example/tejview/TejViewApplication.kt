package com.example.tejview

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.tejview.data.database.HealthDatabase

class TejViewApplication : Application() {

    companion object {
        const val CHANNEL_HEALTH_ALERTS = "health_alerts"
        const val CHANNEL_STEP_TRACKING = "step_tracking"
        const val CHANNEL_WORKOUT = "workout"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // Initialize database eagerly
        HealthDatabase.getDatabase(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Health Alerts Channel (Warning amber color)
            val healthChannel = NotificationChannel(
                CHANNEL_HEALTH_ALERTS,
                "Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical health metric alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                setShowBadge(true)
            }

            // Step Tracking Channel
            val stepChannel = NotificationChannel(
                CHANNEL_STEP_TRACKING,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing step counter notification"
                setShowBadge(false)
            }

            // Workout Channel
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Workout",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Active workout notifications"
            }

            manager.createNotificationChannels(
                listOf(healthChannel, stepChannel, workoutChannel)
            )
        }
    }
}
