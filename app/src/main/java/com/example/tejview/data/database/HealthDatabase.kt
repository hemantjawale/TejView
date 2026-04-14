package com.example.tejview.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tejview.data.dao.*
import com.example.tejview.data.model.*

@Database(
    entities = [
        HealthMetric::class,
        Exercise::class,
        SleepRecord::class,
        MoodEntry::class,
        Medication::class,
        HealthIssue::class,
        ScreenTimeEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun medicationDao(): MedicationDao
    abstract fun healthIssueDao(): HealthIssueDao
    abstract fun screenTimeDao(): ScreenTimeDao

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "tejview_health_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
