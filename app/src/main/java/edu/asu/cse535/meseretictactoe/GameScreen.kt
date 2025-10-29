package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    nav: NavHostController,
    vm: GameViewModel
) {
    val ui by vm.ui.collectAsState()

    val bg = Brush.linearGradient(
        0f to Color(0xFFFEF3C7),
        0.5f to Color(0xFFE9D5FF),
        1f to Color(0xFFBAE6FD)
    )

    val turnText = when (ui.state.playerToMove) {
        Player.X -> "Turn: X"
        Player.O -> "Turn: O"
    }

    val localIsMyTurn =
        if (vm.settings.opponent == Opponent.HUMAN_BT) {
            ui.state.playerToMove == vm.settings.localSide
        } else {
            true
        }

    val statusLine = when (ui.outcome) {
        Outcome.ONGOING -> {
            if (vm.settings.opponent == Opponent.HUMAN_BT) {
                if (localIsMyTurn) {
                    "Your move (${vm.settings.localSide.name})"
                } else {
                    "Opponent's move"
                }
            } else {
                turnText
            }
        }

        Outcome.DRAW -> "Draw"
        Outcome.X_LOSES -> "O wins"
        Outcome.O_LOSES -> "X wins"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(onClick = { nav.navigate(AppRoute.PAST.name) }) {
                        Text("Past")
                    }
                    TextButton(onClick = { nav.navigate(AppRoute.MODE_SELECT.name) }) {
                        Text("Mode")
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Misère Tic-Tac-Toe",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF4CAF50)
                    ),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFBBF7D0),
                                        Color(0xFFA7F3D0)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (vm.settings.opponent == Opponent.HUMAN_BT) {
                                "Two Players • Bluetooth"
                            } else if (vm.settings.opponent == Opponent.HUMAN_LOCAL) {
                                "Two Players • Same Device"
                            } else {
                                "You vs AI (${vm.settings.difficulty.name})"
                            },
                            color = Color(0xFF064E3B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (vm.settings.opponent == Opponent.HUMAN_BT) {
                    Text(
                        text = "You are ${vm.settings.localSide.name} • Opponent is ${
                            if (vm.settings.localSide == Player.X) "O" else "X"
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = if (vm.settings.opponent == Opponent.AI)
                            "You are X • AI is O"
                        else
                            "Two players on this device",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B7280)),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFE5E7EB),
                                        Color(0xFFE0E7FF)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            statusLine,
                            color = Color(0xFF1F2937),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Board(
                    board = ui.state.board,
                    highlight = emptySet(), // fallback if GameState has no highlight
                    enabled = (ui.outcome == Outcome.ONGOING && localIsMyTurn),
                    onTap = { idx -> vm.tap(idx) }
                )

                Spacer(Modifier.height(24.dp))

                PurpleButton(
                    label = "New Game"
                ) {
                    vm.reset()
                }
            }
        }
    }

    if (ui.outcome != Outcome.ONGOING) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(
                    onClick = { vm.reset() }
                ) {
                    Text("Play Again")
                }
            },
            title = {
                Text(
                    when (ui.outcome) {
                        Outcome.DRAW -> "Draw"
                        Outcome.X_LOSES -> "O wins"
                        Outcome.O_LOSES -> "X wins"
                        Outcome.ONGOING -> ""
                    }
                )
            },
            text = {
                Text("Misère rule: 3-in-a-row actually LOSES the game.")
            }
        )
    }
}

@Composable
private fun PurpleButton(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C3A8C)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF5B4BB8),
                            Color(0xFF4A3797)
                        )
                    ),
                    shape
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun Board(
    board: List<Cell>,
    highlight: Set<Int>,
    enabled: Boolean,
    onTap: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0..2) {
                    val idx = r * 3 + c
                    val cell = board[idx]
                    val isHL = highlight.contains(idx)

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(
                                2.dp,
                                if (isHL) Color(0xFFEF4444) else Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isHL) Color(0xFFFFE4E6) else Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = enabled && cell == Cell.EMPTY) {
                                onTap(idx)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val markText = when (cell) {
                            Cell.EMPTY -> ""
                            Cell.X -> "X"
                            Cell.O -> "O"
                        }
                        Text(
                            markText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (cell == Cell.X) Color(0xFFDC2626) else Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}
