package edu.asu.cse535.meseretictactoe

enum class Cell { X, O, EMPTY }
enum class Player { X, O }
enum class Outcome { ONGOING, DRAW, X_LOSES, O_LOSES }

data class GameState(
    val board: List<Cell> = List(9) { Cell.EMPTY },
    val playerToMove: Player = Player.X
) {
    fun moves(): List<Int> = board.indices.filter { board[it] == Cell.EMPTY }
    fun place(i: Int): GameState {
        require(i in 0..8 && board[i] == Cell.EMPTY)
        val b = board.toMutableList()
        b[i] = if (playerToMove == Player.X) Cell.X else Cell.O
        return GameState(b, if (playerToMove == Player.X) Player.O else Player.X)
    }
}

object MisereRules {
    private val LINES = listOf(
        Triple(0,1,2), Triple(3,4,5), Triple(6,7,8),
        Triple(0,3,6), Triple(1,4,7), Triple(2,5,8),
        Triple(0,4,8), Triple(2,4,6)
    )

    private fun hasThree(board: List<Cell>, c: Cell): Boolean =
        LINES.any { (a,b,c2) -> board[a]==c && board[b]==c && board[c2]==c }


    fun outcomeAfter(state: GameState, mover: Player): Outcome {
        val mc = if (mover == Player.X) Cell.X else Cell.O
        if (hasThree(state.board, mc)) {
            return if (mover == Player.X) Outcome.X_LOSES else Outcome.O_LOSES
        }
        return if (state.moves().isEmpty()) Outcome.DRAW else Outcome.ONGOING
    }
}
