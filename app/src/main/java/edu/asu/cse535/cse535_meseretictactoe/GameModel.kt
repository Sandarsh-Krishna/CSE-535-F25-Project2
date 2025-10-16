package edu.asu.cse535.cse535_meseretictactoe

enum class Cell { X, O, E }
enum class Player { X, O }
enum class Outcome { ONGOING, X_LOSES, O_LOSES, DRAW }


data class GameState(
    val board: List<Cell> = List(9) { Cell.E },
    val next: Player = Player.X
) {
    fun moves(): List<Int> = board.indices.filter { board[it] == Cell.E }


    fun place(idx: Int): GameState {
        require(board[idx] == Cell.E) { "Square $idx is not empty" }
        val mark = if (next == Player.X) Cell.X else Cell.O
        val nb = board.toMutableList().apply { this[idx] = mark }
        return copy(board = nb, next = if (next == Player.X) Player.O else Player.X)
    }
}
