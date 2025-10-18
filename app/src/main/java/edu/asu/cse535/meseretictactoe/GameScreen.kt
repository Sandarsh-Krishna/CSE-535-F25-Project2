package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel, nav: NavHostController) {
    val ui by vm.ui.collectAsState()
    val settings = vm.settings
    val aiSide = vm.aiSide

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Misère Tic-Tac-Toe") },
                navigationIcon = { TextButton(onClick = { nav.goBack() }) { Text("Back") } },
                actions = {
                    TextButton(onClick = { nav.navigate(AppRoute.PAST.name) }) { Text("Past") }
                    TextButton(onClick = { nav.navigate(AppRoute.MODE_SELECT.name) }) { Text("Mode") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val modeLabel = when (settings.opponent) {
                Opponent.AI -> "AI (${settings.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}) — You are X"
                Opponent.HUMAN_LOCAL -> "Two Players"
                Opponent.HUMAN_BT -> "Two Players (Bluetooth)"
            }
            Text("Mode: $modeLabel")

            Spacer(Modifier.height(8.dp))
            Text("Turn: ${ui.state.playerToMove}")
            Spacer(Modifier.height(16.dp))

            Board(
                board = ui.state.board,
                enabled = ui.outcome == Outcome.ONGOING &&
                        (aiSide == null || ui.state.playerToMove != aiSide)
            ) { vm.tap(it) }

            Spacer(Modifier.height(16.dp))


            if (ui.outcome != Outcome.ONGOING) {
                Text(
                    text = when (ui.outcome) {
                        Outcome.DRAW -> "Result: Draw"
                        Outcome.X_LOSES -> "Result: X loses"
                        Outcome.O_LOSES -> "Result: O loses"
                        else -> ""
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.reset() }) { Text("New Game") }
        }
    }
}

@Composable
private fun Board(board: List<Cell>, enabled: Boolean, onTap: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (r in 0 until 3) {
            Row {
                for (c in 0 until 3) {
                    val idx = r * 3 + c
                    val label = when (board[idx]) { Cell.X -> "X"; Cell.O -> "O"; Cell.EMPTY -> "" }
                    CellView(label = label, enabled = enabled && board[idx]==Cell.EMPTY) { onTap(idx) }
                }
            }
        }
    }
}

@Composable
private fun CellView(label: String, enabled: Boolean, onTap: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .padding(6.dp)
            .size(90.dp)
            .let { if (enabled) it.clickable { onTap() } else it },
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
    }
}
