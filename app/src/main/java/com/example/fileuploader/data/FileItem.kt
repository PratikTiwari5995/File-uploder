package com.example.fileuploader.data

import android.net.Uri

enum class UploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}

data class FileItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val isPdf: Boolean,
    val originalSize: Long = size,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)