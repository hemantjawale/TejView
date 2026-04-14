package com.example.tejview.ui.mind

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tejview.data.model.MoodEntry
import com.example.tejview.data.model.ScreenTimeEntry
import com.example.tejview.data.repository.HealthRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MindViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    val todayMood = repository.getMoodForDate(today)
    val recentMoods = repository.getRecentMoods(7)
    val todayScreenTime = repository.getScreenTimeForDate(today)

    private val _selectedMood = MutableLiveData<Int?>(null)
    val selectedMood: LiveData<Int?> = _selectedMood

    private val _meditationMinutes = MutableLiveData(0)
    val meditationMinutes: LiveData<Int> = _meditationMinutes

    private val _meditationStreak = MutableLiveData(7)
    val meditationStreak: LiveData<Int> = _meditationStreak

    fun setMood(level: Int) {
        _selectedMood.value = level
        viewModelScope.launch {
            repository.insertMoodEntry(
                MoodEntry(
                    mood = level,
                    stressLevel = mapMoodToStress(level),
                    date = today
                )
            )
        }
    }

    fun saveJournalEntry(text: String) {
        viewModelScope.launch {
            val currentMood = _selectedMood.value ?: 3
            repository.insertMoodEntry(
                MoodEntry(
                    mood = currentMood,
                    stressLevel = mapMoodToStress(currentMood),
                    journalEntry = text,
                    date = today
                )
            )
        }
    }

    fun addMeditationMinutes(minutes: Int) {
        _meditationMinutes.value = (_meditationMinutes.value ?: 0) + minutes
    }

    private fun mapMoodToStress(mood: Int): Int {
        return when (mood) {
            5 -> 15 + (Math.random() * 10).toInt()
            4 -> 25 + (Math.random() * 10).toInt()
            3 -> 40 + (Math.random() * 15).toInt()
            2 -> 60 + (Math.random() * 15).toInt()
            1 -> 75 + (Math.random() * 15).toInt()
            else -> 50
        }
    }

    fun getMoodLabel(level: Int): String {
        return when (level) {
            5 -> "Feeling Great! 😄"
            4 -> "Feeling Good 🙂"
            3 -> "Feeling Okay 😐"
            2 -> "Feeling Low 😔"
            1 -> "Feeling Bad 😢"
            else -> "Tap to log your mood"
        }
    }

    fun getStressLabel(level: Int): String {
        return when {
            level < 30 -> "Low"
            level < 50 -> "Moderate"
            level < 70 -> "Elevated"
            else -> "High"
        }
    }

    fun seedScreenTimeData() {
        viewModelScope.launch {
            repository.insertScreenTime(
                ScreenTimeEntry(
                    totalMinutes = 263,
                    socialMinutes = 105,
                    productiveMinutes = 130,
                    otherMinutes = 28,
                    date = today
                )
            )
        }
    }
}
