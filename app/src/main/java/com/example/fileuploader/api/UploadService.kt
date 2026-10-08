package com.example.fileuploader.api

import com.example.fileuploader.data.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadService {

    @Multipart
    @POST("v2/accounts/{accountId}/uploads/form_data")
    suspend fun uploadFile(
        @Path("accountId") accountId: String,
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

}