package com.example.bluetoothjsontester

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GameHistoryDao {
    @Insert
    suspend fun insert(gameHistory: GameHistory)

    @Query("SELECT * FROM game_history ORDER BY id DESC")
    suspend fun getAll(): List<GameHistory>
}