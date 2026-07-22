package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.BookmarkItem
import com.example.data.models.MedicationItem
import com.example.data.models.WaterLog
import com.example.data.models.PatientRecord
import com.example.data.models.PatientLogEntry
import com.example.data.models.UserNote

@Database(
    entities = [BookmarkItem::class, MedicationItem::class, WaterLog::class, PatientRecord::class, PatientLogEntry::class, UserNote::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_awareness_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
