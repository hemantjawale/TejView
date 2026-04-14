package com.example.tejview.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.tejview.data.database.HealthDatabase
import com.example.tejview.data.model.*

class HealthRepository(application: Application) {
    private val db = HealthDatabase.getDatabase(application)
    private val healthMetricDao = db.healthMetricDao()
    private val exerciseDao = db.exerciseDao()
    private val sleepRecordDao = db.sleepRecordDao()
    private val moodEntryDao = db.moodEntryDao()
    private val medicationDao = db.medicationDao()
    private val healthIssueDao = db.healthIssueDao()
    private val screenTimeDao = db.screenTimeDao()

    // Health Metrics
    suspend fun insertMetric(metric: HealthMetric) = healthMetricDao.insert(metric)
    fun getLatestMetric(type: String, date: String) = healthMetricDao.getLatestMetric(type, date)
    fun getDailyTotal(type: String, date: String) = healthMetricDao.getDailyTotal(type, date)
    fun getMetricsInRange(type: String, start: Long, end: Long) = healthMetricDao.getMetricsInRange(type, start, end)
    fun getRecentMetrics(type: String, limit: Int = 30) = healthMetricDao.getRecentMetrics(type, limit)

    // Exercises
    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insert(exercise)
    fun getExercisesForDate(date: String) = exerciseDao.getExercisesForDate(date)
    fun getRecentExercises(limit: Int = 10) = exerciseDao.getRecentExercises(limit)
    fun getDailyCaloriesBurned(date: String) = exerciseDao.getDailyCaloriesBurned(date)
    fun getDailyActiveMinutes(date: String) = exerciseDao.getDailyActiveMinutes(date)

    // Sleep
    suspend fun insertSleepRecord(record: SleepRecord) = sleepRecordDao.insert(record)
    fun getSleepForDate(date: String) = sleepRecordDao.getSleepForDate(date)
    fun getRecentSleepRecords(limit: Int = 7) = sleepRecordDao.getRecentSleepRecords(limit)
    fun getWeeklyAverageSleep() = sleepRecordDao.getWeeklyAverageSleep()

    // Mood
    suspend fun insertMoodEntry(entry: MoodEntry) = moodEntryDao.insert(entry)
    fun getMoodForDate(date: String) = moodEntryDao.getMoodForDate(date)
    fun getRecentMoods(limit: Int = 7) = moodEntryDao.getRecentMoods(limit)

    // Medications
    suspend fun insertMedication(medication: Medication) = medicationDao.insert(medication)
    fun getMedicationsForDate(date: String) = medicationDao.getMedicationsForDate(date)
    suspend fun updateMedication(medication: Medication) = medicationDao.update(medication)

    // Health Issues
    suspend fun insertHealthIssue(issue: HealthIssue) = healthIssueDao.insert(issue)
    fun getActiveIssues() = healthIssueDao.getActiveIssues()
    fun getRecentIssues(limit: Int = 20) = healthIssueDao.getRecentIssues(limit)
    suspend fun updateHealthIssue(issue: HealthIssue) = healthIssueDao.update(issue)

    // Screen Time
    suspend fun insertScreenTime(entry: ScreenTimeEntry) = screenTimeDao.insert(entry)
    fun getScreenTimeForDate(date: String) = screenTimeDao.getScreenTimeForDate(date)
    fun getRecentScreenTime(limit: Int = 7) = screenTimeDao.getRecentScreenTime(limit)
}
