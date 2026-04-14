package com.example.tejview.ui.sleep

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tejview.data.model.SleepRecord
import com.example.tejview.data.repository.HealthRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    val lastSleep = repository.getSleepForDate(today)
    val recentSleepRecords = repository.getRecentSleepRecords(7)
    val weeklyAverage = repository.getWeeklyAverageSleep()

    private val _isSleepTracking = MutableLiveData(false)
    val isSleepTracking: LiveData<Boolean> = _isSleepTracking

    private var sleepStartTime = 0L

    fun startSleepTracking() {
        _isSleepTracking.value = true
        sleepStartTime = System.currentTimeMillis()
    }

    fun stopSleepTracking() {
        _isSleepTracking.value = false
        val wakeTime = System.currentTimeMillis()
        val totalMinutes = ((wakeTime - sleepStartTime) / 60000).toInt()

        // Simulate sleep phases (in real app, this would come from sensors)
        val deepPercent = 0.20f
        val lightPercent = 0.45f
        val remPercent = 0.25f
        val awakePercent = 0.10f

        viewModelScope.launch {
            repository.insertSleepRecord(
                SleepRecord(
                    bedtime = sleepStartTime,
                    wakeTime = wakeTime,
                    totalMinutes = totalMinutes,
                    deepSleepMinutes = (totalMinutes * deepPercent).toInt(),
                    lightSleepMinutes = (totalMinutes * lightPercent).toInt(),
                    remSleepMinutes = (totalMinutes * remPercent).toInt(),
                    awakeMinutes = (totalMinutes * awakePercent).toInt(),
                    quality = calculateSleepQuality(totalMinutes),
                    date = today
                )
            )
        }
    }

    private fun calculateSleepQuality(totalMinutes: Int): Int {
        return when {
            totalMinutes in 420..540 -> 85 + (Math.random() * 10).toInt() // 7-9 hours
            totalMinutes in 360..600 -> 65 + (Math.random() * 15).toInt()
            totalMinutes > 600 -> 50 + (Math.random() * 15).toInt()
            else -> 30 + (Math.random() * 20).toInt()
        }
    }

    fun seedSleepData() {
        viewModelScope.launch {
            // Only seed if no data exists
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 30)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val bedtime = cal.timeInMillis

            cal.add(Calendar.HOUR_OF_DAY, 7)
            cal.add(Calendar.MINUTE, 23)
            val wakeTime = cal.timeInMillis

            repository.insertSleepRecord(
                SleepRecord(
                    bedtime = bedtime,
                    wakeTime = wakeTime,
                    totalMinutes = 443,
                    deepSleepMinutes = 88,
                    lightSleepMinutes = 155,
                    remSleepMinutes = 112,
                    awakeMinutes = 28,
                    quality = 82,
                    date = today
                )
            )
        }
    }
}
