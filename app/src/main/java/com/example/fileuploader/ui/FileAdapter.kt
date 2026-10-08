package com.example.fileuploader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.fileuploader.data.FileItem
import com.example.fileuploader.data.UploadStatus
import com.example.fileuploader.databinding.ItemFileBinding
import java.util.Locale

class FileAdapter(
    private val onRemoveClicked: (FileItem) -> Unit,
    private val onReplaceClicked: (FileItem) -> Unit,
    private val onFileClicked: (FileItem) -> Unit,
    private val onCompressClicked: (FileItem) -> Unit,
    private val onUploadClick: (FileItem) -> Unit,
) : ListAdapter<FileItem, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FileItem,
            onRemoveClicked: (FileItem) -> Unit,
            onReplaceClicked: (FileItem) -> Unit,
            onFileClicked: (FileItem) -> Unit,
            onCompressClicked: (FileItem) -> Unit,
            onUploadClick: (FileItem) -> Unit
        ) {



            binding.textViewFileName.text = item.name
            if (!item.isPdf && item.size != item.originalSize) {
                binding.textViewFileSize.text =
                    "Original: ${formatFileSize(item.originalSize)}\n" +
                            "Compressed: ${formatFileSize(item.size)}"
            } else {
                binding.textViewFileSize.text =
                    "Size: ${formatFileSize(item.size)}"
            }


            if (item.isPdf) {
                binding.imageViewFile.setImageResource(
                    android.R.drawable.ic_menu_save
                )
            } else {
                binding.imageViewFile.load(item.uri)
            }

            binding.buttonRemove.setOnClickListener {
                onRemoveClicked(item)
            }

            binding.buttonReplace.setOnClickListener {
                onReplaceClicked(item)
            }

            binding.root.setOnClickListener {
                onFileClicked(item)
            }

            if (item.isPdf) {
                binding.buttonCompress.visibility = View.GONE
            } else {
                binding.buttonCompress.visibility = View.VISIBLE
                binding.buttonCompress.setOnClickListener {
                    onCompressClicked(item)
                }
            }
            when (item.uploadStatus) {
                UploadStatus.PENDING -> {
                    showUploadButton()
                    binding.buttonUpload.setOnClickListener {
                        onUploadClick(item)
                    }
                }

                UploadStatus.UPLOADING -> {
                    showUploading()
                }

                UploadStatus.UPLOADED -> {
                    showUploaded()
                }
                UploadStatus.FAILED -> {
                    showUploadButton()
                    binding.buttonUpload.setOnClickListener {
                        onUploadClick(item)
                    }
                }
            }
        }

        private fun showUploadButton() {
            binding.buttonUpload.visibility = View.VISIBLE
            binding.progressUpload.visibility = View.GONE
            binding.textViewUploading.visibility = View.GONE
            binding.textViewUploaded.visibility = View.GONE
        }


        private fun showUploading() {
            binding.buttonUpload.visibility = View.GONE
            binding.progressUpload.visibility = View.VISIBLE
            binding.textViewUploading.visibility = View.VISIBLE
            binding.textViewUploaded.visibility = View.GONE
        }

        private fun showUploaded() {

            binding.buttonUpload.visibility = View.GONE
            binding.progressUpload.visibility = View.GONE
            binding.textViewUploading.visibility = View.GONE
            binding.textViewUploaded.visibility = View.VISIBLE
        }

        private fun formatFileSize(size: Long): String {

            if (size < 1024) {
                return "$size B"
            }

            val kilobytes = size / 1024.0

            if (kilobytes < 1024) {
                return String.format(
                    Locale.US,
                    "%.1f KB",
                    kilobytes
                )
            }

            val megabytes = kilobytes / 1024.0
            return String.format(
                Locale.US,
                "%.1f MB",
                megabytes
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileViewHolder {

        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position),
            onRemoveClicked,
            onReplaceClicked,
            onFileClicked,
            onCompressClicked,
            onUploadClick
        )
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<FileItem>() {
    override fun areItemsTheSame(
        oldItem: FileItem,
        newItem: FileItem
    ): Boolean {
        return oldItem.uri == newItem.uri
    }
    override fun areContentsTheSame(
        oldItem: FileItem,
        newItem: FileItem
    ): Boolean {
        return oldItem == newItem
    }
}