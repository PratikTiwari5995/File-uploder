package com.example.fileuploader.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fileuploader.data.FileItem
import com.example.fileuploader.databinding.ActivityMainBinding
import com.example.fileuploader.ui.pdf.PdfPreviewDialog
import com.example.fileuploader.viewmodel.UploadViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: UploadViewModel by viewModels()

    private lateinit var fileAdapter: FileAdapter

    private var fileBeingReplaced: FileItem? = null



    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->

            uris.forEach { uri ->

                val fileItem = createFileItem(
                    uri = uri,
                    isPdf = false
                )

                viewModel.addFile(fileItem)
            }
        }



    private val pdfPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            uris.forEach { uri ->

                // Keep permission after the app is closed.
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e(
                        "PDF_PERMISSION",
                        "Could not persist PDF permission",
                        e
                    )
                }

                val fileItem = createFileItem(
                    uri = uri,
                    isPdf = true
                )

                viewModel.addFile(fileItem)
            }
        }




    private val replaceImagePicker =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {

                replaceSelectedFile(
                    uri = uri,
                    isPdf = false
                )
            }
        }



    private val replacePdfPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                // Keep permission after the app is closed.
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e(
                        "PDF_PERMISSION",
                        "Could not persist replacement PDF permission",
                        e
                    )
                }

                replaceSelectedFile(
                    uri = uri,
                    isPdf = true
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupClickListeners()
        observeFiles()
        observeUploadResult()
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onRemoveClicked = { fileItem ->
                viewModel.removeFile(fileItem)
            },

            onReplaceClicked = { fileItem ->
                startReplace(fileItem)
            },

            onFileClicked = { fileItem ->

                if (fileItem.isPdf) {
                    showPdfPreview(fileItem)
                }
            },

            onCompressClicked = { fileItem ->
                viewModel.compressFile(fileItem)
            },

            onUploadClick = { fileItem ->
                viewModel.uploadFile(fileItem)
            }
        )

        binding.recyclerViewFiles.apply {

            layoutManager =
                LinearLayoutManager(this@MainActivity)

            adapter = fileAdapter
        }
    }


    private fun setupClickListeners() {

        binding.buttonPickImages.setOnClickListener {

            imagePicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }


        binding.buttonPickPdfs.setOnClickListener {

            pdfPicker.launch(
                arrayOf("application/pdf")
            )
        }


        binding.buttonHistory.setOnClickListener {

            val intent = Intent(
                this,
                HistoryActivity::class.java
            )

            startActivity(intent)
        }
    }



    private fun observeFiles() {

        viewModel.files.observe(this) { files ->

            fileAdapter.submitList(files)
        }
    }


    private fun startReplace(fileItem: FileItem) {

        fileBeingReplaced = fileItem

        if (fileItem.isPdf) {

            replacePdfPicker.launch(
                arrayOf("application/pdf")
            )

        } else {

            replaceImagePicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }



    private fun replaceSelectedFile(
        uri: Uri,
        isPdf: Boolean
    ) {

        val oldFile =
            fileBeingReplaced ?: return

        val newFile = createFileItem(
            uri = uri,
            isPdf = isPdf
        )

        viewModel.replaceFile(
            oldFile = oldFile,
            newFile = newFile
        )

        fileBeingReplaced = null
    }



    private fun showPdfPreview(fileItem: FileItem) {

        val dialog = PdfPreviewDialog.newInstance(
            fileItem.uri
        )

        dialog.show(
            supportFragmentManager,
            "PdfPreviewDialog"
        )
    }



    private fun createFileItem(
        uri: Uri,
        isPdf: Boolean
    ): FileItem {

        return FileItem(
            uri = uri,
            name = getFileName(uri),
            size = getFileSize(uri),
            isPdf = isPdf
        )
    }



    private fun getFileName(uri: Uri): String {

        var fileName = "Unknown file"

        val cursor: Cursor? =
            contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.DISPLAY_NAME
                ),
                null,
                null,
                null
            )

        cursor?.use {

            if (it.moveToFirst()) {

                val nameIndex =
                    it.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (nameIndex >= 0) {
                    fileName =
                        it.getString(nameIndex)
                }
            }
        }

        return fileName
    }




    private fun observeUploadResult() {

        viewModel.uploadResult.observe(this) { result ->

            Log.d(
                "UPLOAD_TEST",
                "Result received: $result"
            )

            result ?: return@observe

            if (result.success) {

                Toast.makeText(
                    this,
                    "Upload successful:\n${result.url}",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this,
                    result.errorMessage
                        ?: "Upload failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    private fun getFileSize(uri: Uri): Long {

        var fileSize = 0L

        val cursor: Cursor? =
            contentResolver.query(
                uri,
                arrayOf(
                    OpenableColumns.SIZE
                ),
                null,
                null,
                null
            )

        cursor?.use {

            if (it.moveToFirst()) {

                val sizeIndex =
                    it.getColumnIndex(
                        OpenableColumns.SIZE
                    )

                if (
                    sizeIndex >= 0 &&
                    !it.isNull(sizeIndex)
                ) {
                    fileSize =
                        it.getLong(sizeIndex)
                }
            }
        }

        return fileSize
    }
}