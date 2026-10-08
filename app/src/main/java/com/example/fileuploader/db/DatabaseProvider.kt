package com.example.fileuploader.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var database: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "file_uploader_database"
            ).build().also {
                database = it
            }
        }
    }
}