package edu.asu.cse535.meseretictactoe




private val LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
    listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
)


private fun hasThree(board: List<Cell>, c: Cell): Boolean =
    LINES.any { (a, b, d) -> board[a] == c && board[b] == c && board[d] == c }


