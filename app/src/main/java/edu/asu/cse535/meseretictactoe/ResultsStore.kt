package edu.asu.cse535.meseretictactoe

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class PastGame(
    val timeMillis: Long,
    val difficulty: Difficulty,
    val outcome: Outcome
)

object ResultsStore {
    private const val FILE_NAME = "past_games.json"
    private var appContext: Context? = null
    private val _items = mutableListOf<PastGame>()
    val items: List<PastGame> get() = _items

    fun init(ctx: Context) {
        if (appContext == null) {
            appContext = ctx.applicationContext
            loadFromDisk()
        }
    }

    fun add(item: PastGame) {
        _items += item
        saveToDisk()
    }

    fun clear() {
        _items.clear()
        saveToDisk()
    }

    private fun loadFromDisk() {
        val c = appContext ?: return
        val f = File(c.filesDir, FILE_NAME)
        if (!f.exists()) return
        val text = runCatching { f.readText() }.getOrNull() ?: return
        val arr = runCatching { JSONArray(text) }.getOrNull() ?: return
        _items.clear()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val t = obj.optLong("timeMillis", 0L)
            val diffStr = obj.optString("difficulty", "EASY")
            val outStr = obj.optString("outcome", "ONGOING")
            val diff = when (diffStr) {
                "EASY" -> Difficulty.EASY
                "MEDIUM" -> Difficulty.MEDIUM
                "HARD" -> Difficulty.HARD
                else -> Difficulty.EASY
            }
            val out = when (outStr) {
                "X_LOSES" -> Outcome.X_LOSES
                "O_LOSES" -> Outcome.O_LOSES
                "DRAW" -> Outcome.DRAW
                "ONGOING" -> Outcome.ONGOING
                else -> Outcome.ONGOING
            }
            _items.add(
                PastGame(
                    timeMillis = t,
                    difficulty = diff,
                    outcome = out
                )
            )
        }
    }

    private fun saveToDisk() {
        val c = appContext ?: return
        val f = File(c.filesDir, FILE_NAME)
        val arr = JSONArray()
        for (pg in _items) {
            val obj = JSONObject()
            obj.put("timeMillis", pg.timeMillis)
            obj.put("difficulty", pg.difficulty.name)
            obj.put("outcome", pg.outcome.name)
            arr.put(obj)
        }
        runCatching { f.writeText(arr.toString()) }
    }
}
