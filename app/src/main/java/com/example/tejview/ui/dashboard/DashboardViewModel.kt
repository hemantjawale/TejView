package com.example.tejview.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tejview.data.model.HealthMetric
import com.example.tejview.data.repository.HealthRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    // LiveData from database
    val heartRate = repository.getLatestMetric("heart_rate", today)
    val spo2 = repository.getLatestMetric("spo2", today)
    val dailySteps = repository.getDailyTotal("steps", today)
    val dailyCalories = repository.getDailyTotal("calories", today)
    val dailyActiveMinutes = repository.getDailyActiveMinutes(today)
    val dailyWater = repository.getDailyTotal("water", today)
    val dailyProtein = repository.getDailyTotal("protein", today)
    val lastSleep = repository.getSleepForDate(today)

    // Serenity Score (computed from various metrics)
    private val _serenityScore = MutableLiveData(87)
    val serenityScore: LiveData<Int> = _serenityScore

    private val _serenityStatus = MutableLiveData("Excellent")
    val serenityStatus: LiveData<String> = _serenityStatus

    fun addWater() {
        viewModelScope.launch {
            repository.insertMetric(
                HealthMetric(
                    type = "water",
                    value = 1f,
                    unit = "glass",
                    date = today
                )
            )
        }
    }

    fun addProtein(grams: Float) {
        viewModelScope.launch {
            repository.insertMetric(
                HealthMetric(
                    type = "protein",
                    value = grams,
                    unit = "g",
                    date = today
                )
            )
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            // Seed heart rate
            repository.insertMetric(
                HealthMetric(type = "heart_rate", value = 72f, unit = "bpm", date = today)
            )
            // Seed SpO2
            repository.insertMetric(
                HealthMetric(type = "spo2", value = 98f, unit = "%", date = today)
            )
            // Seed steps
            repository.insertMetric(
                HealthMetric(type = "steps", value = 8432f, unit = "steps", date = today)
            )
            // Seed calories
            repository.insertMetric(
                HealthMetric(type = "calories", value = 1847f, unit = "cal", date = today)
            )
            // Seed water
            for (i in 1..6) {
                repository.insertMetric(
                    HealthMetric(type = "water", value = 1f, unit = "glass", date = today)
                )
            }
            // Seed protein
            repository.insertMetric(
                HealthMetric(type = "protein", value = 78f, unit = "g", date = today)
            )
            // Seed sleep
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 30)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val bedtime = cal.timeInMillis
            cal.add(Calendar.HOUR_OF_DAY, 7)
            cal.add(Calendar.MINUTE, 23)
            val wakeTime = cal.timeInMillis

            repository.insertSleepRecord(
                com.example.tejview.data.model.SleepRecord(
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

    fun calculateSerenityScore(
        heartRate: Float?,
        spo2: Float?,
        steps: Float?,
        sleepMinutes: Int?,
        mood: Int?
    ) {
        var score = 50 // Base

        // Heart rate contribution (60-80 optimal)
        heartRate?.let {
            score += when {
                it in 60f..80f -> 15
                it in 50f..90f -> 10
                else -> 0
            }
        }

        // SpO2 contribution (95+ optimal)
        spo2?.let {
            score += when {
                it >= 97f -> 15
                it >= 95f -> 10
                it >= 90f -> 5
                else -> 0
            }
        }

        // Steps contribution
        steps?.let {
            score += when {
                it >= 10000f -> 10
                it >= 7000f -> 8
                it >= 5000f -> 5
                else -> 2
            }
        }

        // Sleep contribution
        sleepMinutes?.let {
            score += when {
                it in 420..540 -> 10 // 7-9 hours
                it in 360..600 -> 5
                else -> 0
            }
        }

        _serenityScore.postValue(score.coerceIn(0, 100))
        _serenityStatus.postValue(
            when {
                score >= 85 -> "Excellent"
                score >= 70 -> "Good"
                score >= 50 -> "Fair"
                else -> "Needs Attention"
            }
        )
    }
}
