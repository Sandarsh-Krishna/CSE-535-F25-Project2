package edu.asu.cse535.cse535_meseretictactoe

import org.junit.Test
import kotlin.test.assertTrue

class DifficultyTest {

    @Test
    fun easyAlwaysLegal() {
        val s = stateOf(".X.......", Player.O)
        repeat(50) {
            val m = chooseAIMove(s, Difficulty.EASY)
            assertTrue(m in s.moves())
        }
    }

    @Test
    fun hardMatchesBestMove() {
        val s = stateOf(".X.......", Player.O)
        val hard = chooseAIMove(s, Difficulty.HARD)
        val best = bestMoveMisere(s)
        assertTrue(hard == best, "HARD must choose optimal move")
    }

    @Test
    fun mediumSometimesRandomSometimesOptimal() {
        val s = stateOf("X........", Player.O)
        val best = bestMoveMisere(s)
        var bestCount = 0
        var nonBestCount = 0
        repeat(100) {
            val m = chooseAIMove(s, Difficulty.MEDIUM)
            if (m == best) bestCount++ else nonBestCount++
        }
        // Heuristic: both buckets should be nonzero most of the time
        assertTrue(bestCount in 20..80 && nonBestCount in 20..80,
            "MEDIUM should mix random and optimal roughly 50/50")
    }
}
