package com.example.tejview.ui.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tejview.data.model.HealthIssue
import com.example.tejview.data.model.HealthMetric
import com.example.tejview.data.model.Medication
import com.example.tejview.data.repository.HealthRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    val heartRate = repository.getLatestMetric("heart_rate", today)
    val dailyWater = repository.getDailyTotal("water", today)
    val dailyProtein = repository.getDailyTotal("protein", today)
    val medications = repository.getMedicationsForDate(today)
    val activeIssues = repository.getActiveIssues()

    fun addWater() {
        viewModelScope.launch {
            repository.insertMetric(
                HealthMetric(type = "water", value = 1f, unit = "glass", date = today)
            )
        }
    }

    fun addProtein(grams: Float) {
        viewModelScope.launch {
            repository.insertMetric(
                HealthMetric(type = "protein", value = grams, unit = "g", date = today)
            )
        }
    }

    fun logMedication(name: String, dosage: String) {
        viewModelScope.launch {
            repository.insertMedication(
                Medication(name = name, dosage = dosage, taken = true, date = today)
            )
        }
    }

    fun reportHealthIssue(title: String, description: String, severity: String) {
        viewModelScope.launch {
            repository.insertHealthIssue(
                HealthIssue(
                    title = title,
                    description = description,
                    severity = severity,
                    date = today
                )
            )
        }
    }

    fun resolveIssue(issue: HealthIssue) {
        viewModelScope.launch {
            repository.updateHealthIssue(issue.copy(resolved = true))
        }
    }
}
