package com.example.fileuploader.db

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return value?.toUri()
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }
}