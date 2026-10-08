package com.example.fileuploader.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fileuploader.data.FileEntity

@Dao
interface  FileDao {
    @Insert
    suspend fun insert(file: FileEntity)

    @Delete
    suspend fun delete(file: FileEntity)

    @Query("DELETE FROM files WHERE uri = :uri")
    suspend fun deleteByUri(uri: Uri)

    @Query("SELECT * FROM files ORDER BY id ASC")
    suspend fun getAllFiles(): List<FileEntity>

    @Query("SELECT * FROM files WHERE uri = :uri LIMIT 1")
    suspend fun getFileByUri(uri: Uri): FileEntity?

    @Query("""
    UPDATE files
    SET uploadUrl = :uploadUrl,
        uploadPath = :uploadPath,
        etag = :etag,
        accountId = :accountId
    WHERE uri = :uri""")
    suspend fun updateUploadInfo(
        uri: Uri,
        uploadUrl: String?,
        uploadPath: String?,
        etag: String?,
        accountId: String?
    )
}