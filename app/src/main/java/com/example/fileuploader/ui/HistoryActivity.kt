package com.example.fileuploader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fileuploader.data.FileEntity
import com.example.fileuploader.databinding.ActivityHistoryBinding
import com.example.fileuploader.db.DatabaseProvider
import com.example.fileuploader.repository.FileRepository
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val database = DatabaseProvider.getDatabase(
                applicationContext
            )
            val repository = FileRepository(
                database.fileDao()
            )
            val files: List<FileEntity> =
                repository.getAllFiles()
            historyAdapter.submitList(files)
        }
    }
}