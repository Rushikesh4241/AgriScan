package com.example.agriscan.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class, Prediction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
}
