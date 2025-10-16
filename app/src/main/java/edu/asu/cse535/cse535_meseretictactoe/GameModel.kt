package edu.asu.cse535.cse535_meseretictactoe

enum class Cell { X, O, E }
enum class Player { X, O }
enum class Outcome { ONGOING, X_LOSES, O_LOSES, DRAW }

/** Immutable 3×3 board (flat list of 9) + whose turn is next. */
data class GameState(
    val board: List<Cell> = List(9) { Cell.E },
    val next: Player = Player.X
) {
    /** Empty squares you can play (indices 0..8). */
    fun moves(): List<Int> = board.indices.filter { board[it] == Cell.E }

    /** Place current player's mark at [idx] and flip the turn. */
    fun place(idx: Int): GameState {
        require(board[idx] == Cell.E) { "Square $idx is not empty" }
        val mark = if (next == Player.X) Cell.X else Cell.O
        val nb = board.toMutableList().apply { this[idx] = mark }
        return copy(board = nb, next = if (next == Player.X) Player.O else Player.X)
    }
}
