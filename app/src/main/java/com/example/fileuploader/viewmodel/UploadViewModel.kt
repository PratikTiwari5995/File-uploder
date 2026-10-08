package com.example.fileuploader.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fileuploader.api.BytescaleUploadService
import com.example.fileuploader.data.FileItem
import com.example.fileuploader.data.FileEntity
import com.example.fileuploader.data.ImageCompressor
import com.example.fileuploader.data.UploadResult
import com.example.fileuploader.data.UploadStatus
import com.example.fileuploader.db.DatabaseProvider
import com.example.fileuploader.repository.FileRepository
import kotlinx.coroutines.launch

class UploadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _files = MutableLiveData<List<FileItem>>(emptyList())
    val files: LiveData<List<FileItem>> = _files

    private val _uploadResult = MutableLiveData<UploadResult?>()
    val uploadResult: LiveData<UploadResult?> = _uploadResult

    private val imageCompressor = ImageCompressor(application)

    private val repository: FileRepository

    private val uploadService = BytescaleUploadService(application)

    init {
        val database = DatabaseProvider.getDatabase(application)
        repository = FileRepository(database.fileDao())

        loadFiles()
    }

    fun addFile(fileItem: FileItem) {
        val currentFiles = _files.value ?: emptyList()

        val alreadyExists = currentFiles.any { existingFile ->
            existingFile.uri == fileItem.uri
        }

        if (alreadyExists) {
            return
        }

        _files.value = currentFiles + fileItem

        saveFile(fileItem)
    }

    private fun saveFile(fileItem: FileItem) {
        viewModelScope.launch {

            val entity = FileEntity(
                name = fileItem.name,
                uri = fileItem.uri,
                size = fileItem.size,
                isPdf = fileItem.isPdf
            )

            repository.insert(entity)
        }
    }

    fun removeFile(fileItem: FileItem) {
        val currentFiles = _files.value ?: emptyList()

        _files.value = currentFiles.filter { existingFile ->
            existingFile.uri != fileItem.uri
        }
    }

    fun replaceFile(
        oldFile: FileItem,
        newFile: FileItem
    ) {
        val currentFiles = _files.value ?: emptyList()

        val oldIndex = currentFiles.indexOfFirst { fileItem ->
            fileItem.uri == oldFile.uri
        }

        if (oldIndex == -1) {
            return
        }

        val updatedFiles = currentFiles.toMutableList()
        updatedFiles[oldIndex] = newFile

        _files.value = updatedFiles
    }

    fun compressFile(fileItem: FileItem) {
        if (fileItem.isPdf) {
            return
        }

        viewModelScope.launch {

            val compressedFile = imageCompressor.compress(
                uri = fileItem.uri
            )

            val compressedFileItem = fileItem.copy(
                uri = Uri.fromFile(compressedFile),
                size = compressedFile.length()
            )

            val currentFiles = _files.value ?: emptyList()

            val index = currentFiles.indexOfFirst {
                it.uri == fileItem.uri
            }

            if (index == -1) {
                return@launch
            }

            val updatedFiles = currentFiles.toMutableList()
            updatedFiles[index] = compressedFileItem

            _files.value = updatedFiles
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {

            val entities = repository.getAllFiles()

            val fileItems = entities.map { entity ->
                FileItem(
                    uri = entity.uri,
                    name = entity.name,
                    size = entity.size,
                    isPdf = entity.isPdf
                )
            }

            _files.value = fileItems
        }
    }

    fun uploadFile(fileItem: FileItem) {

        viewModelScope.launch {

            // 1. Tell the UI that uploading has started.
            updateUploadStatus(
                fileItem = fileItem,
                status = UploadStatus.UPLOADING
            )

            // 2. Perform the actual Retrofit upload.
            val result = uploadService.upload(
                uri = fileItem.uri,
                fileName = fileItem.name
            )

            _uploadResult.value = result

            // 3. Check whether the upload succeeded.
            if (result.success) {

                updateUploadStatus(
                    fileItem = fileItem,
                    status = UploadStatus.UPLOADED
                )

                repository.updateUploadInfo(
                    uri = fileItem.uri,
                    uploadUrl = result.url,
                    uploadPath = result.path,
                    etag = result.etag,
                    accountId = result.accountId
                )

            } else {

                updateUploadStatus(
                    fileItem = fileItem,
                    status = UploadStatus.FAILED
                )
            }
        }
    }

    private fun updateUploadStatus(
        fileItem: FileItem,
        status: UploadStatus
    ) {
        val currentFiles = _files.value ?: return
        val updatedFiles = currentFiles.map { currentFile ->
            if (currentFile.uri == fileItem.uri) {

                currentFile.copy(
                    uploadStatus = status
                )

            } else {

                currentFile
            }
        }

        _files.value = updatedFiles
    }
}