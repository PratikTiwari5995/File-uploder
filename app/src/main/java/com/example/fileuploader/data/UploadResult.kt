package com.example.fileuploader.data



data class UploadResult(
    val success: Boolean,
    val url: String? = null,
    val path: String? = null,
    val etag: String? = null,
    val accountId: String? = null,
    val errorMessage: String? = null
)