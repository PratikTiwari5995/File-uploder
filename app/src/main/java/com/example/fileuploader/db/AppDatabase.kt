package com.example.fileuploader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fileuploader.data.FileEntity

@Database(
    entities = [FileEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase()  {

    abstract fun fileDao(): FileDao
}