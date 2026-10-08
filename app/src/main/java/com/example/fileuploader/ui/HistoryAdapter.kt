package com.example.fileuploader.ui

import android.R
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fileuploader.data.FileEntity
import com.example.fileuploader.databinding.ItemHistoryBinding

class HistoryAdapter :
    ListAdapter<FileEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: FileEntity) {

            binding.textViewHistoryName.text = file.name

            binding.textViewHistorySize.text =
                formatFileSize(file.size)

            binding.textViewHistoryUrl.text =
                file.uploadUrl ?: "Not uploaded"

            if (file.isPdf) {
                binding.imageViewHistoryFile.setImageResource(
                    R.drawable.ic_menu_view
                )
            } else {
                binding.imageViewHistoryFile.setImageResource(
                    R.drawable.ic_menu_gallery
                )
            }
            binding.textViewHistoryUrl.setOnClickListener {

                val url = file.uploadUrl

                if (url.isNullOrBlank()) {
                    Toast.makeText(
                        binding.root.context,
                        "This file has not been uploaded",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val request = DownloadManager.Request(
                    Uri.parse(url)
                )
                    .setTitle(file.name)
                    .setDescription("Downloading ${file.name}")
                    .setMimeType(
                        if (file.isPdf) {
                            "application/pdf"
                        } else {
                            "application/octet-stream"
                        }
                    )
                    .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        file.name
                    )

                val downloadManager =
                    binding.root.context.getSystemService(
                        Context.DOWNLOAD_SERVICE
                    ) as DownloadManager

                downloadManager.enqueue(request)

                Toast.makeText(
                    binding.root.context,
                    "Download started",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 ->
                    "${size / 1024} KB"
                else ->
                    "%.2f MB".format(
                        size.toDouble() / (1024 * 1024)
                    )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FileEntity>() {

        override fun areItemsTheSame(
            oldItem: FileEntity,
            newItem: FileEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FileEntity,
            newItem: FileEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}