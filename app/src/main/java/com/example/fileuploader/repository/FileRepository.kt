package com.example.fileuploader.repository

import android.net.Uri
import com.example.fileuploader.db.FileDao
import com.example.fileuploader.data.FileEntity

class FileRepository(
    private val fileDao: FileDao
) {

    suspend fun insert(file: FileEntity) {
        fileDao.insert(file)
    }

    suspend fun delete(file: FileEntity) {
        fileDao.delete(file)
    }

    suspend fun deleteByUri(uri: Uri) {
        fileDao.deleteByUri(uri)

    }

    suspend fun getAllFiles(): List<FileEntity> {
        return fileDao.getAllFiles()
    }

    suspend fun getFileByUri(uri: Uri): FileEntity? {
        return fileDao.getFileByUri(uri)
    }

    suspend fun updateUploadInfo(
        uri: Uri,
        uploadUrl: String?,
        uploadPath: String?,
        etag: String?,
        accountId: String?
    ){
        fileDao.updateUploadInfo(
            uri = uri,
            uploadUrl = uploadUrl,
            uploadPath = uploadPath,
            etag = etag,
            accountId = accountId
        )
    }

}