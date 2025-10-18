package edu.asu.cse535.meseretictactoe

data class PastGame(
    val timeMillis: Long,
    val difficulty: Difficulty,
    val outcome: Outcome
)

object ResultsStore {
    private val _items = mutableListOf<PastGame>()
    val items: List<PastGame> get() = _items

    fun add(item: PastGame) { _items += item }
    fun clear() { _items.clear() }
}
