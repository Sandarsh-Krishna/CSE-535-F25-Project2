package edu.asu.cse535.cse535_meseretictactoe

import org.junit.Test
import kotlin.test.assertEquals

class RulesTest {

    @Test
    fun moverLosesIfTheyCreateThreeInARow() {
        // Top row: "XX." → X plays at idx 2, creates "XXX" and loses in misère.
        val before = stateOf("XX.......", Player.X)  // 9 chars: 2 X + 7 dots
        val after  = before.place(2)                 // X to idx 2
        val out    = outcomeAfterMove(before, after)
        assertEquals(Outcome.X_LOSES, out)
    }

    @Test
    fun noThreeAndNoEmptyMeansDraw() {
        // One empty at idx 8; X plays there without forming 3-in-a-row → full board → DRAW.
        // Board indices:
        // 0 1 2   X O X
        // 3 4 5   X O O
        // 6 7 8   O X .
        val before = stateOf("XOXXOOOX.", Player.X)
        val after  = before.place(8) // X fills last cell; does not create XXX anywhere
        val out    = outcomeAfterMove(before, after)
        assertEquals(Outcome.DRAW, out)
    }

    @Test
    fun ongoingIfNoLossAndSpacesRemain() {
        val before = stateOf("X........", Player.O)
        val after  = before.place(4) // O in center, no 3-in-a-row
        val out    = outcomeAfterMove(before, after)
        assertEquals(Outcome.ONGOING, out)
    }
}
