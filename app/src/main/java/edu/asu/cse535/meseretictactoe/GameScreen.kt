package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material3.TopAppBarDefaults
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    nav: NavHostController,
    vm: GameViewModel
) {
    val ui by vm.ui.collectAsState()

    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF141A3A),
            Color(0xFF3B1A78)
        )
    )

    val localIsMyTurn =
        if (ui.opponent == Opponent.HUMAN_BT) {
            ui.state.playerToMove == vm.settings.localSide
        } else {
            true
        }

    val isFinished = ui.outcome != Outcome.ONGOING

    val resultTitle = when (ui.outcome) {
        Outcome.DRAW -> "Draw"
        Outcome.X_LOSES -> "O wins"
        Outcome.O_LOSES -> "X wins"
        Outcome.ONGOING -> ""
    }

    val localWonMessage = run {
        when (ui.outcome) {
            Outcome.ONGOING -> ""
            Outcome.DRAW -> "Draw"
            Outcome.X_LOSES -> { // O wins
                if (vm.settings.localSide == Player.O) "You win" else "O wins"
            }
            Outcome.O_LOSES -> { // X wins
                if (vm.settings.localSide == Player.X) "You win" else "X wins"
            }
        }
    }

    val turnChipText = when {
        isFinished -> localWonMessage.ifBlank { resultTitle }
        else -> "Turn: ${ui.state.playerToMove.name}"
    }

    val modeChipText = when (ui.opponent) {
        Opponent.AI -> {
            val diffLabel = ui.difficulty.name.lowercase()
                .replaceFirstChar { it.uppercaseChar() }
            "AI • $diffLabel • You're ${vm.settings.localSide.name}"
        }
        Opponent.HUMAN_LOCAL -> {
            "2P • Same Device • You're ${vm.settings.localSide.name}"
        }
        Opponent.HUMAN_BT -> {
            "2P • Bluetooth • You're ${vm.settings.localSide.name}"
        }
    }

    fun goToModeSelect() {
        nav.navigate(AppRoute.MODE_SELECT.name) {
            popUpTo(AppRoute.MODE_SELECT.name) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(
                        onClick = { goToModeSelect() }
                    ) {
                        Text(
                            "Back",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        nav.navigate(AppRoute.PAST.name)
                    }) {
                        Text(
                            "Past",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    TextButton(onClick = {
                        goToModeSelect()
                    }) {
                        Text(
                            "Mode",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(gradientBg)
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(gradientBg),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F172A).copy(alpha = 0.35f),
                                    Color(0xFF0F172A).copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Heading
                        Text(
                            "Misère Tic-Tac-Toe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        PillChip(
                            text = modeChipText,
                            bgGradient = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.12f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.12f)
                                )
                            ),
                            borderColor = Color(0xFF6366F1).copy(alpha = 0.8f),
                            textColor = Color(0xFFE2E8F0)
                        )

                        Spacer(Modifier.height(12.dp))

                        if (ui.opponent == Opponent.AI) {
                            DifficultyRowChips(
                                current = ui.difficulty,
                                onSelect = { newDiff ->
                                    vm.setDifficulty(newDiff)
                                }
                            )

                            Spacer(Modifier.height(12.dp))
                        }

                        PillChip(
                            text = turnChipText,
                            bgGradient = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFF97316).copy(alpha = 0.15f),
                                    Color(0xFFFBBF24).copy(alpha = 0.08f)
                                )
                            ),
                            borderColor = Color(0xFFF97316).copy(alpha = 0.6f),
                            textColor = Color(0xFFFFEDD5)
                        )

                        Spacer(Modifier.height(20.dp))

                        BoardGrid(
                            board = ui.state.board,
                            highlight = emptySet(),
                            enabled = (!isFinished && localIsMyTurn),
                            onTap = { idx -> vm.tap(idx) }
                        )

                        Spacer(Modifier.height(24.dp))

                        if (isFinished) {
                            ResultChip(
                                label = localWonMessage.ifBlank { resultTitle }
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        if (ui.opponent == Opponent.HUMAN_BT) {
                            NewGameButton(
                                label = "Choose Starter",
                                onClick = {

                                    nav.navigate(AppRoute.P2P.name)
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        NewGameButton(
                            label = if (isFinished) "Play Again" else "New Game",
                            onClick = { vm.reset() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillChip(
    text: String,
    bgGradient: Brush,
    borderColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            borderColor
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    bgGradient,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DifficultyRowChips(
    current: Difficulty,
    onSelect: (Difficulty) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DiffChip(
            label = "Easy",
            active = current == Difficulty.EASY,
            onClick = { onSelect(Difficulty.EASY) }
        )
        DiffChip(
            label = "Medium",
            active = current == Difficulty.MEDIUM,
            onClick = { onSelect(Difficulty.MEDIUM) }
        )
        DiffChip(
            label = "Hard",
            active = current == Difficulty.HARD,
            onClick = { onSelect(Difficulty.HARD) }
        )
    }
}

@Composable
private fun DiffChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val borderColor = Color(0xFF4C1D95)
    val shape = RoundedCornerShape(10.dp)
    val bgBrush = if (active) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF6366F1).copy(alpha = 0.2f),
                Color(0xFF8B5CF6).copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF0F172A).copy(alpha = 0.35f),
                Color(0xFF0F172A).copy(alpha = 0.15f)
            )
        )
    }

    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) Color(0xFF6366F1) else Color(0xFF94A3B8).copy(alpha = 0.35f)
        ),
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush, shape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (active) Color.White else Color(0xFFE2E8F0),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BoardGrid(
    board: List<Cell>,
    highlight: Set<Int>,
    enabled: Boolean,
    onTap: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (r in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0..2) {
                    val idx = r * 3 + c
                    val cell = board[idx]
                    val isHL = highlight.contains(idx)

                    val tileBg = Brush.verticalGradient(
                        listOf(
                            Color(0xFF101827),
                            Color(0xFF111827)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .border(
                                width = 2.dp,
                                color = if (isHL) Color(0xFFF97316) else Color(0xFF1F2937),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                brush = tileBg,
                                shape = RoundedCornerShape(10.dp)
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
                        val markColor = when (cell) {
                            Cell.X -> Color(0xFFF43F5E)   // pinkish red
                            Cell.O -> Color(0xFF38BDF8)
                            Cell.EMPTY -> Color.Transparent
                        }

                        Text(
                            text = markText,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = markColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultChip(
    label: String
) {
    val chipBg = Brush.horizontalGradient(
        listOf(
            Color(0xFF6366F1).copy(alpha = 0.55f),   // was ~0.15f
            Color(0xFF8B5CF6).copy(alpha = 0.55f)
        )
    )

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF93C5FD)
        )
    ) {
        Box(
            modifier = Modifier
                .background(chipBg, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NewGameButton(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val btnBg = Brush.horizontalGradient(
        listOf(
            Color(0xFF6366F1),
            Color(0xFF8B5CF6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 140.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() },
            shape = shape,
            color = Color.Transparent,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF4C1D95)
            )
        ) {
            Box(
                modifier = Modifier
                    .background(btnBg, shape)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
