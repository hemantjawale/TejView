package com.example.tejview.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,       // "heart_rate", "spo2", "steps", "calories", "active_minutes", "water", "protein", "weight", "blood_pressure"
    val value: Float,
    val secondaryValue: Float? = null, // For blood pressure (diastolic)
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String        // "yyyy-MM-dd" format for easy daily grouping
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,       // "running", "cycling", "yoga", "gym", "swimming", "walking"
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val distanceKm: Float? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedtime: Long,      // Timestamp when went to bed
    val wakeTime: Long,     // Timestamp when woke up
    val totalMinutes: Int,
    val deepSleepMinutes: Int,
    val lightSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeMinutes: Int,
    val quality: Int,       // 0-100 score
    val date: String
)

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mood: Int,          // 1=Bad, 2=Low, 3=Neutral, 4=Good, 5=Great
    val stressLevel: Int,   // 0-100
    val journalEntry: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val taken: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)

@Entity(tableName = "health_issues")
data class HealthIssue(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val severity: String,   // "low", "medium", "high"
    val resolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)

@Entity(tableName = "screen_time")
data class ScreenTimeEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalMinutes: Int,
    val socialMinutes: Int,
    val productiveMinutes: Int,
    val otherMinutes: Int,
    val date: String
)
