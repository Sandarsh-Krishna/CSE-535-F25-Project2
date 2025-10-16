package edu.asu.cse535.cse535_meseretictactoe

import kotlin.math.max
import kotlin.math.min

/** Pair a move index with its Minimax score (from O's perspective). */
data class ScoredMove(val idx: Int, val score: Int)

/**
 * Returns the best move index for the AI (player O) using Minimax + alpha–beta pruning.
 * On a 3×3 board, maxDepth = 9 gives perfect play.
 */
fun bestMoveMisere(state: GameState, maxDepth: Int = 9): Int {
    val legal = state.moves()
    if (legal.isEmpty()) return -1

    var best = ScoredMove(idx = legal.first(), score = Int.MIN_VALUE)
    var alpha = Int.MIN_VALUE
    var beta  = Int.MAX_VALUE

    for (m in legal) {
        val ns = state.place(m)
        val score = when (outcomeAfterMove(state, ns)) {
            Outcome.O_LOSES -> -1
            Outcome.X_LOSES ->  1
            Outcome.DRAW    ->  0
            Outcome.ONGOING -> minimax(
                state = ns,
                depth = 1,
                alphaIn = alpha,
                betaIn = beta,
                maximizing = (ns.next == Player.O), // O maximizes
                maxDepth = maxDepth
            )
        }
        if (score > best.score) best = ScoredMove(m, score)
        alpha = max(alpha, score)
        if (beta <= alpha) break // alpha–beta prune
    }
    return best.idx
}

/** Minimax search with alpha–beta pruning (internal so tests in this module can access). */
internal fun minimax(
    state: GameState,
    depth: Int,
    alphaIn: Int,
    betaIn: Int,
    maximizing: Boolean,
    maxDepth: Int
): Int {
    var alpha = alphaIn
    var beta = betaIn

    if (depth >= maxDepth) return 0

    val legal = state.moves()
    if (legal.isEmpty()) return 0

    var best = if (maximizing) Int.MIN_VALUE else Int.MAX_VALUE

    for (m in legal) {
        val ns = state.place(m)
        val score = when (outcomeAfterMove(state, ns)) {
            Outcome.O_LOSES -> -1
            Outcome.X_LOSES ->  1
            Outcome.DRAW    ->  0
            Outcome.ONGOING -> minimax(
                state = ns,
                depth = depth + 1,
                alphaIn = alpha,
                betaIn = beta,
                maximizing = (ns.next == Player.O),
                maxDepth = maxDepth
            )
        }

        if (maximizing) {
            best = max(best, score)
            alpha = max(alpha, best)
            if (beta <= alpha) break
        } else {
            best = min(best, score)
            beta = min(beta, best)
            if (beta <= alpha) break
        }
    }
    return best
}

/** Public helper for tests: score child state from O's perspective. */
fun scoreForO(
    parent: GameState,
    child: GameState,
    depth: Int = 1,
    maxDepth: Int = 9
): Int = when (outcomeAfterMove(parent, child)) {
    Outcome.O_LOSES -> -1
    Outcome.X_LOSES ->  1
    Outcome.DRAW    ->  0
    Outcome.ONGOING -> minimax(
        state = child,
        depth = depth,
        alphaIn = Int.MIN_VALUE,
        betaIn = Int.MAX_VALUE,
        maximizing = (child.next == Player.O),
        maxDepth = maxDepth
    )
}
