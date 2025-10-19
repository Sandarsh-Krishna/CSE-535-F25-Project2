package com.example.bluetoothjsontester

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val winner: String,
    val board: String // JSON string of the board state
)