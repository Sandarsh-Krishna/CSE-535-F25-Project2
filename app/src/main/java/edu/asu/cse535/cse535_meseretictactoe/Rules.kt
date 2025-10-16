package edu.asu.cse535.cse535_meseretictactoe

// NOTE: Replace the package above with *your exact* project package.
// From your message: package edu.asu.cse535.cse535_meseretictactactoe

// 3×3 index map (row-major):
// 0 1 2
// 3 4 5
// 6 7 8

/** All 8 line triplets used to detect three-in-a-row. */
private val LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
    listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
)

/** True if mark [c] currently has any three-in-a-row on [board]. */
private fun hasThree(board: List<Cell>, c: Cell): Boolean =
    LINES.any { (a, b, d) -> board[a] == c && board[b] == c && board[d] == c }

/**
 * Misère Tic-Tac-Toe rule evaluation for the move that produced [nextState] from [prev].
 *
 * - If the **mover** created a 3-in-a-row with their own mark, the mover **loses**.
 * - Else if no empty cells remain, it’s a **draw**.
 * - Otherwise the game is **ongoing**.
 */
fun outcomeAfterMove(prev: GameState, nextState: GameState): Outcome {
    val mover = prev.next
    val moverCell = if (mover == Player.X) Cell.X else Cell.O

    // Misère twist: the player who makes three-in-a-row loses immediately.
    if (hasThree(nextState.board, moverCell)) {
        return if (mover == Player.X) Outcome.X_LOSES else Outcome.O_LOSES
    }

    // No empty squares left -> draw.
    if (nextState.board.none { it == Cell.E }) return Outcome.DRAW

    // Otherwise keep playing.
    return Outcome.ONGOING
}
