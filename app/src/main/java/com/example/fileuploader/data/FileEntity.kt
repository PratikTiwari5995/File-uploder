package com.example.fileuploader.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val uri: Uri,
    val size: Long,
    val isPdf: Boolean,
    val uploadUrl: String? = null,
    val uploadPath: String? = null,
    val etag: String? = null,
    val accountId: String? = null
)