package edu.asu.cse535.meseretictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Difficulty { EASY, MEDIUM, HARD }
enum class Opponent { AI, HUMAN_LOCAL, HUMAN_BT }

data class GameSettings(
    val opponent: Opponent = Opponent.AI,
    val difficulty: Difficulty = Difficulty.EASY,
    val starter: Player = Player.X,
    val localSide: Player = Player.X
)

data class UiState(
    val state: GameState = GameState(),
    val outcome: Outcome = Outcome.ONGOING,
    val difficulty: Difficulty = Difficulty.EASY,
    val opponent: Opponent = Opponent.AI
)

class GameViewModel : ViewModel() {

    private val _ui = MutableStateFlow(
        UiState(
            state = GameState(),
            outcome = Outcome.ONGOING,
            difficulty = Difficulty.EASY,
            opponent = Opponent.AI
        )
    )
    val ui: StateFlow<UiState> = _ui

    var settings: GameSettings = GameSettings()
        private set

    val aiSide: Player?
        get() = if (settings.opponent == Opponent.AI) Player.O else null

    init {
        viewModelScope.launch {
            P2PSession.incoming.collectLatest { msg ->
                when {
                    msg.startsWith("MOVE:") -> {
                        val idx = msg.removePrefix("MOVE:").toIntOrNull() ?: return@collectLatest
                        applyPeerMove(idx)
                    }

                    msg == "RESET" -> {
                        resetInternal(sync = false)
                    }

                    msg.startsWith("SYNC:") -> {
                        val parts = msg.split(":")
                        val starterFromSync =
                            if (parts.getOrNull(1) == "X") Player.X else Player.O
                        val hostSideStr = parts.getOrNull(2) ?: "X"
                        val joinerSideStr = parts.getOrNull(3) ?: "O"

                        val mySide =
                            if (P2PSession.amHost == true) {
                                if (hostSideStr == "X") Player.X else Player.O
                            } else {
                                if (joinerSideStr == "X") Player.X else Player.O
                            }

                        settings = settings.copy(
                            opponent = Opponent.HUMAN_BT,
                            difficulty = settings.difficulty,
                            starter = starterFromSync,
                            localSide = mySide
                        )

                        _ui.update {
                            it.copy(
                                difficulty = settings.difficulty,
                                opponent = settings.opponent
                            )
                        }

                        resetInternal(sync = false)
                    }
                }
            }
        }
    }

    fun applySettings(s: GameSettings) {
        when (s.opponent) {
            Opponent.AI -> {
                settings = s.copy(
                    starter = Player.X,
                    localSide = Player.X,
                    opponent = Opponent.AI
                )
            }

            Opponent.HUMAN_LOCAL -> {
                settings = s.copy(
                    starter = Player.X,
                    localSide = Player.X,
                    opponent = Opponent.HUMAN_LOCAL
                )
            }

            Opponent.HUMAN_BT -> {
                settings = settings.copy(
                    opponent = Opponent.HUMAN_BT,
                    difficulty = s.difficulty,
                    starter = s.starter,
                    localSide = s.localSide
                )
            }
        }

        _ui.update {
            it.copy(
                difficulty = settings.difficulty,
                opponent = settings.opponent
            )
        }
    }

    fun setDifficulty(newDiff: Difficulty) {
        if (settings.opponent == Opponent.AI) {
            settings = settings.copy(difficulty = newDiff)
            _ui.update {
                it.copy(
                    difficulty = settings.difficulty,
                    opponent = settings.opponent
                )
            }
        }
    }

    fun reset() = resetInternal(sync = true)

    private fun resetInternal(sync: Boolean) {
        val startPlayer =
            if (settings.opponent == Opponent.AI) Player.X
            else settings.starter

        _ui.value = UiState(
            state = GameState(playerToMove = startPlayer),
            outcome = Outcome.ONGOING,
            difficulty = settings.difficulty,
            opponent = settings.opponent
        )

        if (sync && settings.opponent == Opponent.HUMAN_BT) {
            P2PSession.send("RESET")
        }
    }

    fun tap(index: Int) {
        if (_ui.value.outcome != Outcome.ONGOING) return
        val cur = _ui.value.state

        if (settings.opponent == Opponent.HUMAN_BT) {
            if (cur.playerToMove != settings.localSide) {
                return
            }
        }

        if (index !in cur.moves()) return
        val next = cur.place(index)
        evaluateAfterMove(cur, next)

        if (
            settings.opponent == Opponent.AI &&
            _ui.value.outcome == Outcome.ONGOING &&
            _ui.value.state.playerToMove == aiSide
        ) {
            aiMove()
        }

        if (settings.opponent == Opponent.HUMAN_BT) {
            P2PSession.send("MOVE:$index")
        }
    }

    private fun applyPeerMove(index: Int) {
        val cur = _ui.value.state
        if (_ui.value.outcome != Outcome.ONGOING) return
        if (index !in cur.moves()) return
        val next = cur.place(index)
        evaluateAfterMove(cur, next)
    }

    private fun evaluateAfterMove(prev: GameState, next: GameState) {
        val mover = prev.playerToMove
        val outcome = MisereRules.outcomeAfter(next, mover)

        _ui.update {
            it.copy(
                state = next,
                outcome = outcome,
                difficulty = settings.difficulty,
                opponent = settings.opponent
            )
        }

        if (outcome != Outcome.ONGOING) {
            ResultsStore.add(
                PastGame(
                    timeMillis = System.currentTimeMillis(),
                    difficulty = settings.difficulty,
                    outcome = outcome,
                    opponent = settings.opponent
                )
            )
        }
    }

    fun aiMove() {
        val cur = _ui.value.state
        val legal = cur.moves()
        if (legal.isEmpty()) return

        val i = when (settings.difficulty) {
            Difficulty.EASY -> easyMove(cur)
            Difficulty.MEDIUM -> mediumMove(cur)
            Difficulty.HARD -> hardMove(cur)
        }

        val next = cur.place(i)
        evaluateAfterMove(cur, next)
    }

    private fun easyMove(state: GameState): Int =
        state.moves().random()

    private fun mediumMove(state: GameState): Int {
        val legal = state.moves()
        val me = state.playerToMove
        val safe = legal.filter { idx ->
            val s2 = state.place(idx)
            MisereRules.outcomeAfter(s2, me) == Outcome.ONGOING
        }
        return (safe.ifEmpty { legal }).random()
    }

    private val memo = HashMap<Pair<List<Cell>, Player>, Int>()

    private fun hardMove(state: GameState): Int {
        var bestScore = Int.MIN_VALUE
        var bestIdx = state.moves().first()
        for (i in state.moves()) {
            val s2 = state.place(i)
            val score = -minimax(s2, justMoved = state.playerToMove)
            if (score > bestScore) {
                bestScore = score
                bestIdx = i
            }
            if (bestScore == 1) break
        }
        return bestIdx
    }

    private fun minimax(state: GameState, justMoved: Player): Int {
        when (val out = MisereRules.outcomeAfter(state, justMoved)) {
            Outcome.X_LOSES -> return if (justMoved == Player.X) +1 else -1
            Outcome.O_LOSES -> return if (justMoved == Player.O) +1 else -1
            Outcome.DRAW -> return 0
            Outcome.ONGOING -> { /* keep searching */ }
        }
        val key = Pair(state.board, state.playerToMove)
        memo[key]?.let { return it }

        var best = Int.MIN_VALUE
        for (i in state.moves()) {
            val s2 = state.place(i)
            val score = -minimax(s2, justMoved = state.playerToMove)
            if (score > best) best = score
            if (best == 1) break
        }
        memo[key] = best
        return best
    }
}
