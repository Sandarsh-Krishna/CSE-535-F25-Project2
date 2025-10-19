package com.example.bluetoothjsontester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var lvHistory: ListView
    private lateinit var gameHistoryDao: GameHistoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        lvHistory = findViewById(R.id.lvHistory)
        gameHistoryDao = AppDatabase.getDatabase(this).gameHistoryDao()

        lifecycleScope.launch {
            val gameHistory = gameHistoryDao.getAll()
            val adapter = ArrayAdapter(this@HistoryActivity, android.R.layout.simple_list_item_1, gameHistory.map { it.toString() })
            lvHistory.adapter = adapter
        }
    }
}
