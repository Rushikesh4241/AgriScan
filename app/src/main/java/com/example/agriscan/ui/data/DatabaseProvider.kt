package com.example.agriscan.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "agriscan_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        return INSTANCE!!
    }
}
