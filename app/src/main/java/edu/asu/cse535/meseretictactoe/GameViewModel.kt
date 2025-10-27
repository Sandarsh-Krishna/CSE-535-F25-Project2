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
    val outcome: Outcome = Outcome.ONGOING
)

class GameViewModel : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    var settings: GameSettings = GameSettings()
        private set


    val aiSide: Player? get() =
        if (settings.opponent == Opponent.AI) Player.O else null

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
                        val starterFromPeer =
                            if (parts.getOrNull(1) == "starterX") Player.X else Player.O
                        val peerClaimsSide =
                            if (parts.getOrNull(2) == "iamX") Player.X else Player.O


                        val mySide = if (peerClaimsSide == Player.X) Player.O else Player.X

                        settings = settings.copy(
                            opponent = Opponent.HUMAN_BT,
                            starter = starterFromPeer,
                            localSide = mySide
                        )

                        resetInternal(sync = false)
                    }
                }
            }
        }
    }


    fun applySettings(s: GameSettings) {
        when (s.opponent) {
            Opponent.AI -> {
                // Force X starts, local player is X
                settings = s.copy(
                    starter = Player.X,
                    localSide = Player.X,
                    opponent = Opponent.AI
                )
            }

            Opponent.HUMAN_LOCAL -> {
                // Same phone 2 players
                settings = s.copy(
                    localSide = Player.X,
                    opponent = Opponent.HUMAN_LOCAL
                )
            }

            Opponent.HUMAN_BT -> {
                // Bluetooth peer-to-peer
                val amHost = (P2PSession.amHost == true)
                val mySide = if (amHost) Player.X else Player.O
                val startPlayer = Player.X // X always goes first

                settings = s.copy(
                    starter = startPlayer,
                    localSide = mySide,
                    opponent = Opponent.HUMAN_BT
                )


                val syncMsg =
                    "SYNC:" +
                            (if (startPlayer == Player.X) "starterX" else "starterO") +
                            ":iam" + (if (mySide == Player.X) "X" else "O")
                P2PSession.send(syncMsg)
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
            outcome = Outcome.ONGOING
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
                // It's not your turn, ignore the tap.
                return
            }
        }

        if (index !in cur.moves()) return

        val next = cur.place(index)
        evaluateAfterMove(cur, next)


        if (settings.opponent == Opponent.AI &&
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

        _ui.update { it.copy(state = next, outcome = outcome) }

        if (outcome != Outcome.ONGOING) {
            // You already have ResultsStore.add(PastGame(...))
            ResultsStore.add(
                PastGame(
                    timeMillis = System.currentTimeMillis(),
                    difficulty = settings.difficulty,
                    outcome = outcome
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
            Outcome.ONGOING -> { /* keep going */ }
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
