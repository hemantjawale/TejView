package com.example.tejview.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.tejview.data.model.*

@Dao
interface HealthMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: HealthMetric)

    @Query("SELECT * FROM health_metrics WHERE date = :date AND type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMetric(type: String, date: String): LiveData<HealthMetric?>

    @Query("SELECT * FROM health_metrics WHERE date = :date AND type = :type ORDER BY timestamp DESC")
    fun getMetricsForDate(type: String, date: String): LiveData<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE type = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMetrics(type: String, limit: Int = 30): LiveData<List<HealthMetric>>

    @Query("SELECT SUM(value) FROM health_metrics WHERE date = :date AND type = :type")
    fun getDailyTotal(type: String, date: String): LiveData<Float?>

    @Query("SELECT * FROM health_metrics WHERE type = :type AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getMetricsInRange(type: String, startTime: Long, endTime: Long): LiveData<List<HealthMetric>>

    @Delete
    suspend fun delete(metric: HealthMetric)
}

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE date = :date ORDER BY timestamp DESC")
    fun getExercisesForDate(date: String): LiveData<List<Exercise>>

    @Query("SELECT * FROM exercises ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentExercises(limit: Int = 10): LiveData<List<Exercise>>

    @Query("SELECT SUM(caloriesBurned) FROM exercises WHERE date = :date")
    fun getDailyCaloriesBurned(date: String): LiveData<Int?>

    @Query("SELECT SUM(durationMinutes) FROM exercises WHERE date = :date")
    fun getDailyActiveMinutes(date: String): LiveData<Int?>

    @Delete
    suspend fun delete(exercise: Exercise)
}

@Dao
interface SleepRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SleepRecord)

    @Query("SELECT * FROM sleep_records WHERE date = :date LIMIT 1")
    fun getSleepForDate(date: String): LiveData<SleepRecord?>

    @Query("SELECT * FROM sleep_records ORDER BY date DESC LIMIT :limit")
    fun getRecentSleepRecords(limit: Int = 7): LiveData<List<SleepRecord>>

    @Query("SELECT AVG(totalMinutes) FROM sleep_records ORDER BY date DESC LIMIT 7")
    fun getWeeklyAverageSleep(): LiveData<Float?>

    @Delete
    suspend fun delete(record: SleepRecord)
}

@Dao
interface MoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry)

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getMoodForDate(date: String): LiveData<MoodEntry?>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMoods(limit: Int = 7): LiveData<List<MoodEntry>>

    @Delete
    suspend fun delete(entry: MoodEntry)
}

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Query("SELECT * FROM medications WHERE date = :date ORDER BY timestamp DESC")
    fun getMedicationsForDate(date: String): LiveData<List<Medication>>

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)
}

@Dao
interface HealthIssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: HealthIssue)

    @Query("SELECT * FROM health_issues WHERE resolved = 0 ORDER BY timestamp DESC")
    fun getActiveIssues(): LiveData<List<HealthIssue>>

    @Query("SELECT * FROM health_issues ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIssues(limit: Int = 20): LiveData<List<HealthIssue>>

    @Update
    suspend fun update(issue: HealthIssue)

    @Delete
    suspend fun delete(issue: HealthIssue)
}

@Dao
interface ScreenTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ScreenTimeEntry)

    @Query("SELECT * FROM screen_time WHERE date = :date LIMIT 1")
    fun getScreenTimeForDate(date: String): LiveData<ScreenTimeEntry?>

    @Query("SELECT * FROM screen_time ORDER BY date DESC LIMIT :limit")
    fun getRecentScreenTime(limit: Int = 7): LiveData<List<ScreenTimeEntry>>
}
