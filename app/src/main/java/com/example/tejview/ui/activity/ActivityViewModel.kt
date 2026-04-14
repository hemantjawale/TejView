package com.example.tejview.ui.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tejview.data.model.Exercise
import com.example.tejview.data.model.HealthMetric
import com.example.tejview.data.repository.HealthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    val recentExercises = repository.getRecentExercises(10)
    val dailySteps = repository.getDailyTotal("steps", today)
    val dailyCalories = repository.getDailyCaloriesBurned(today)
    val dailyActiveMinutes = repository.getDailyActiveMinutes(today)

    // Workout Timer
    private val _isWorkoutActive = MutableLiveData(false)
    val isWorkoutActive: LiveData<Boolean> = _isWorkoutActive

    private val _workoutType = MutableLiveData("")
    val workoutType: LiveData<String> = _workoutType

    private val _workoutTimer = MutableLiveData("00:00:00")
    val workoutTimer: LiveData<String> = _workoutTimer

    private var timerJob: Job? = null
    private var workoutStartTime = 0L
    private var isPaused = false

    fun startWorkout(type: String) {
        _workoutType.value = type
        _isWorkoutActive.value = true
        workoutStartTime = System.currentTimeMillis()
        isPaused = false

        timerJob = viewModelScope.launch {
            while (true) {
                if (!isPaused) {
                    val elapsed = System.currentTimeMillis() - workoutStartTime
                    val hours = (elapsed / 3600000).toInt()
                    val minutes = ((elapsed % 3600000) / 60000).toInt()
                    val seconds = ((elapsed % 60000) / 1000).toInt()
                    _workoutTimer.postValue(String.format("%02d:%02d:%02d", hours, minutes, seconds))
                }
                delay(1000)
            }
        }
    }

    fun pauseWorkout() {
        isPaused = !isPaused
    }

    fun stopWorkout() {
        timerJob?.cancel()
        _isWorkoutActive.value = false

        val elapsed = System.currentTimeMillis() - workoutStartTime
        val durationMinutes = (elapsed / 60000).toInt().coerceAtLeast(1)
        val caloriesEstimate = (durationMinutes * getCaloriesPerMinute(_workoutType.value ?: "walking")).toInt()

        viewModelScope.launch {
            repository.insertExercise(
                Exercise(
                    type = _workoutType.value ?: "unknown",
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesEstimate,
                    distanceKm = if (_workoutType.value in listOf("running", "cycling", "walking"))
                        durationMinutes * 0.15f else null,
                    date = today
                )
            )
        }
    }

    fun logExercise(type: String, durationMinutes: Int) {
        val calories = (durationMinutes * getCaloriesPerMinute(type)).toInt()
        viewModelScope.launch {
            repository.insertExercise(
                Exercise(
                    type = type,
                    durationMinutes = durationMinutes,
                    caloriesBurned = calories,
                    distanceKm = if (type in listOf("running", "cycling", "walking"))
                        durationMinutes * 0.15f else null,
                    date = today
                )
            )
        }
    }

    private fun getCaloriesPerMinute(type: String): Float {
        return when (type) {
            "running" -> 11f
            "cycling" -> 8f
            "swimming" -> 10f
            "gym" -> 7f
            "yoga" -> 4f
            "walking" -> 5f
            else -> 5f
        }
    }

    fun seedExerciseData() {
        viewModelScope.launch {
            repository.insertExercise(
                Exercise(
                    type = "running",
                    durationMinutes = 32,
                    caloriesBurned = 285,
                    distanceKm = 4.2f,
                    date = today,
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )
            repository.insertExercise(
                Exercise(
                    type = "yoga",
                    durationMinutes = 45,
                    caloriesBurned = 180,
                    date = today,
                    timestamp = System.currentTimeMillis() - 14400000
                )
            )
            repository.insertExercise(
                Exercise(
                    type = "gym",
                    durationMinutes = 60,
                    caloriesBurned = 420,
                    date = today,
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }
}
