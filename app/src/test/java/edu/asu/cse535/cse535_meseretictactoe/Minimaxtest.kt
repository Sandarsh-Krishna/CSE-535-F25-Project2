package edu.asu.cse535.cse535_meseretictactoe

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MinimaxTest {

    @Test
    fun aiAvoidsImmediateSelfLoss() {
        // X to move set up a trap where O could accidentally finish a line on its own next move.
        // Board (indexes):
        // 0 1 2
        // 3 4 5
        // 6 7 8
        //
        // Pattern leaves situations where some O placements would create OOO (and thus O would lose).
        val s = stateOf(
            // X at 0 and 1, O at 3 and 6; it is O to move
            // . . .
            // O . .
            // O . .
            "XX.....OO".reversed(), // just to vary; use any pattern you like, but keep it legal
            Player.O
        )
        val move = bestMoveMisere(s)
        // Ensure AI chooses a legal move and DOES NOT pick an immediately losing one.
        assertTrue(move in s.moves())
        val ns = s.place(move)
        val out = outcomeAfterMove(s, ns)
        assertTrue(out != Outcome.O_LOSES, "AI must not lose immediately")
    }

    @Test
    fun aiPrefersHumanLossOverDraw() {
        // Construct a position where one move leads to X losing next state (good for O),
        // and another leads to a draw. AI should pick the human-loss branch (+1).
        // Example simple case: X to move just created a fork where O can force X_LOSES.
        val s = stateOf(
            // X . X
            // . O .
            // . . .
            "X.X.O....",
            Player.O
        )
        val move = bestMoveMisere(s)
        assertTrue(move in s.moves())
        // We can’t guarantee exact index in a synthetic example, but we can check score:
        // Re-evaluate all legal moves and confirm chosen move has the maximal score.
        val bestScore = s.moves().maxOf { m ->
            val ns = s.place(m)
            when (outcomeAfterMove(s, ns)) {
                Outcome.X_LOSES -> 1
                Outcome.O_LOSES -> -1
                Outcome.DRAW    -> 0
                Outcome.ONGOING -> minimax(
                    ns, 1, Int.MIN_VALUE, Int.MAX_VALUE, ns.next == Player.O, 9
                )
            }
        }
        val chosenScore = run {
            val ns = s.place(move)
            when (outcomeAfterMove(s, ns)) {
                Outcome.X_LOSES -> 1
                Outcome.O_LOSES -> -1
                Outcome.DRAW    -> 0
                Outcome.ONGOING -> minimax(
                    ns, 1, Int.MIN_VALUE, Int.MAX_VALUE, ns.next == Player.O, 9
                )
            }
        }
        assertEquals(bestScore, chosenScore, "Chosen move must be optimal")
    }

    @Test
    fun aiReturnsSomeMoveWhenOnlyOneLeft() {
        val s = stateOf("XOXXOOXX.", Player.X) // last empty is idx 8, but X moves; then O must respond
        val afterX = s.place(8)                  // human fills last, outcomeAfterMove says DRAW
        // O won't even be called here in real game, but bestMove should handle empty-moves gracefully:
        val move = bestMoveMisere(afterX)
        assertTrue(move == -1 || move in afterX.moves())
    }
}
