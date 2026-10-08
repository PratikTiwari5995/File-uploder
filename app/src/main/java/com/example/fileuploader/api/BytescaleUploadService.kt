package com.example.fileuploader.api

import android.content.Context
import android.net.Uri
import com.example.fileuploader.BuildConfig
import com.example.fileuploader.data.UploadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class BytescaleUploadService(private val context: Context) {

    suspend fun upload(uri: Uri, fileName: String): UploadResult = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext UploadResult(success = false, errorMessage = "Unable to open file")

            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            val part = MultipartBody.Part.createFormData(
                "file", fileName, bytes.toRequestBody(mimeType.toMediaType())
            )

            val response = ApiClient.uploadService.uploadFile(
                accountId = BuildConfig.BYTESCALE_ACCOUNT_ID,
                authorization = "Bearer ${BuildConfig.BYTESCALE_API_KEY}",
                file = part
            )


            val file = response.body()?.files?.firstOrNull()
            if (response.isSuccessful && file != null) {
                UploadResult(
                    success = true,
                    url = file.fileUrl,
                    path = file.filePath,
                    etag = file.etag,
                    accountId = file.accountId
                )
            } else {
                UploadResult(
                    success = false,
                    errorMessage = "Upload failed: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            UploadResult(success = false, errorMessage = e.message ?: "Network error")
        }
    }
}