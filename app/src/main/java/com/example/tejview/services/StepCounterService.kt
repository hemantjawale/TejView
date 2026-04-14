package com.example.tejview.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.tejview.MainActivity
import com.example.tejview.R
import com.example.tejview.TejViewApplication
import com.example.tejview.data.database.HealthDatabase
import com.example.tejview.data.model.HealthMetric
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Foreground service that uses the hardware step counter sensor
 * to track daily steps in real-time.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var initialSteps = -1f
    private var currentSteps = 0f

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification(0))

        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if (initialSteps < 0) {
                    initialSteps = it.values[0]
                }
                currentSteps = it.values[0] - initialSteps

                // Update notification
                val notification = createNotification(currentSteps.toInt())
                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(NOTIFICATION_ID, notification)

                // Save to database periodically (every 50 steps)
                if (currentSteps.toInt() % 50 == 0) {
                    saveStepsToDatabase(currentSteps)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    private fun saveStepsToDatabase(steps: Float) {
        serviceScope.launch {
            val db = HealthDatabase.getDatabase(applicationContext)
            val today = dateFormat.format(Date())
            db.healthMetricDao().insert(
                HealthMetric(
                    type = "steps",
                    value = steps,
                    unit = "steps",
                    date = today
                )
            )
        }
    }

    private fun createNotification(steps: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TejViewApplication.CHANNEL_STEP_TRACKING)
            .setContentTitle("TejView Step Tracker")
            .setContentText("$steps steps today")
            .setSmallIcon(R.drawable.ic_activity)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        // Save final step count
        saveStepsToDatabase(currentSteps)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
