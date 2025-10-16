package edu.asu.cse535.cse535_meseretictactoe

import kotlin.random.Random

enum class Difficulty { EASY, MEDIUM, HARD }

fun chooseAIMove(state: GameState, difficulty: Difficulty): Int {
    val legal = state.moves()
    if (legal.isEmpty()) return -1
    return when (difficulty) {
        Difficulty.EASY   -> legal.random()                          // always random
        Difficulty.MEDIUM -> if (Random.nextBoolean()) legal.random() // ~50% random
        else bestMoveMisere(state)
        Difficulty.HARD   -> bestMoveMisere(state)                    // always optimal
    }
}
