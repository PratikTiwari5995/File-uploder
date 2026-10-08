package com.example.fileuploader.data

data class UploadResponse(
    val files: List<UploadedFileResponse>
)

data class UploadedFileResponse(
    val formDataFieldName: String,
    val accountId: String,
    val etag: String,
    val filePath: String,
    val fileUrl: String
)