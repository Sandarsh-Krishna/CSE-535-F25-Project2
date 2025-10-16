package edu.asu.cse535.cse535_meseretictactoe

import org.junit.Test
import kotlin.test.assertTrue

class AiSmokeTest {

    @Test
    fun aiReturnsLegalMoveOnEmptyBoardAfterHumanCenter() {
        // Human (X) plays center (idx 4), AI (O) should move legally
        var s = GameState()
        s = s.place(4) // human X at center
        val aiMove = bestMoveMisere(s)
        assertTrue(aiMove in s.moves(), "AI move must be legal")
    }
}
