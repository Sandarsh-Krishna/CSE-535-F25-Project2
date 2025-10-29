package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel, nav: NavHostController) {
    val ui by vm.ui.collectAsState()
    val settingsSnapshot = vm.settings
    val aiSide = vm.aiSide

    var localDifficulty by remember { mutableStateOf(settingsSnapshot.difficulty) }

    val bg = Brush.linearGradient(
        colors = listOf(Luxe.bgStart, Luxe.bgEnd),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ){
    Scaffold(
        topBar = {
            // CHANGE: Center title, transparent bar for a cleaner hierarchy
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Game",
                        fontWeight = FontWeight.SemiBold,
                        color = Luxe.textPrimary // CHANGE: ensure contrast on dark bg
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { nav.goBack() }) {
                        Text("Back", color = Luxe.textPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { nav.navigate(AppRoute.PAST.name) }) {
                        Text("Past", color = Luxe.textPrimary)
                    }
                    TextButton(onClick = { nav.navigate(AppRoute.MODE_SELECT.name) }) {
                        Text("Mode", color = Luxe.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Luxe.glass, // CHANGE
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, Luxe.glassBorder), // CHANGE
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Misère Tic-Tac-Toe",

                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Luxe.textPrimary,
                        letterSpacing = 0.2.sp,
                    )

                    Spacer(Modifier.height(12.dp))

                    ModePill(
                        opponent = settingsSnapshot.opponent,
                        difficulty = localDifficulty
                    )

                    if (settingsSnapshot.opponent == Opponent.AI) {
                        Spacer(Modifier.height(12.dp))
                        DifficultyRow(
                            current = localDifficulty,
                            onSelect = { newDiff ->
                                localDifficulty = newDiff
                                vm.setDifficulty(newDiff)
                            }
                        )
                    }

                    if (settingsSnapshot.opponent == Opponent.HUMAN_BT) {
                        Spacer(Modifier.height(8.dp))
                        val me = settingsSnapshot.localSide
                        val them = if (me == Player.X) Player.O else Player.X
                        Text(
                            text = "You are $me • Opponent is $them",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Luxe.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    TurnChip(player = ui.state.playerToMove)

                    Spacer(Modifier.height(16.dp))

                    Board(
                        board = ui.state.board,

                        enabled =
                            ui.outcome == Outcome.ONGOING &&
                                    (aiSide == null || ui.state.playerToMove != aiSide) &&
                                    (settingsSnapshot.opponent != Opponent.HUMAN_BT ||
                                            ui.state.playerToMove == settingsSnapshot.localSide)
                    ) { vm.tap(it) }

                    Spacer(Modifier.height(16.dp))

                    ResultBanner(ui.outcome, settingsSnapshot.opponent)

                    Spacer(Modifier.height(20.dp))

                    PrimaryButton(
                        label = "New Game",
                        onClick = { vm.reset() }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ModePill(
    opponent: Opponent,
    difficulty: Difficulty
) {
    val label = when (opponent) {
        Opponent.AI -> {
            val diff = difficulty.name.lowercase()
                .replaceFirstChar { it.uppercase() }
            "AI • $diff • You’re X"
        }
        Opponent.HUMAN_LOCAL -> "Two Players • Same Device"
        Opponent.HUMAN_BT -> "Two Players • Bluetooth"
    }

    val shape = RoundedCornerShape(999.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        contentColor = Color(0xFF065F46),
        border = BorderStroke(1.dp, Luxe.chipOutline)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DifficultyRow(
    current: Difficulty,
    onSelect: (Difficulty) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectableChip(
            label = "Easy",
            active = current == Difficulty.EASY,
            onClick = { onSelect(Difficulty.EASY) }
        )
        SelectableChip(
            label = "Medium",
            active = current == Difficulty.MEDIUM,
            onClick = { onSelect(Difficulty.MEDIUM) }
        )
        SelectableChip(
            label = "Hard",
            active = current == Difficulty.HARD,
            onClick = { onSelect(Difficulty.HARD) }
        )
    }
}

@Composable
private fun SelectableChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (active) {
        Brush.horizontalGradient(listOf(Luxe.accent1.copy(alpha = 0.9f), Luxe.accent2.copy(alpha = 0.9f)))
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val borderCol = if (active) Luxe.accent2 else Luxe.chipOutline
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = shape,
        border = BorderStroke(1.dp, borderCol),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(bg, shape)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (active) Color.White else Luxe.textPrimary, // CHANGE
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = shape,
        border = BorderStroke(1.dp, Color(0x334C3A8C)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier

                .background(
                    Brush.horizontalGradient(listOf(Luxe.accent1, Luxe.accent2)),
                    shape
                )
                .padding(horizontal = 22.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
private fun TurnChip(player: Player) {
    val (bg, fg, borderC) = if (player == Player.X)
        Triple(Color(0xFFFFEDD5), Color(0xFF9A3412), Color(0xFF9A3412).copy(alpha = 0.25f))
    else
        Triple(Color(0xFFE0E7FF), Color(0xFF3730A3), Color(0xFF3730A3).copy(alpha = 0.25f))

    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = Color(0x26FFFFFF),
        contentColor = Luxe.textPrimary,
        border = BorderStroke(1.dp, Luxe.tileBorder)
    ) {
        Text(
            text = "Turn: $player",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun Board(
    board: List<Cell>,
    enabled: Boolean,
    onTap: (Int) -> Unit
) {
    val tileColor = Luxe.tileFill
    val gap = 12.dp

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val tileSize: Dp = (maxWidth - gap * 2) / 3

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                ) {
                    for (c in 0 until 3) {
                        val idx = r * 3 + c
                        val label = when (board[idx]) {
                            Cell.X -> "X"
                            Cell.O -> "O"
                            Cell.EMPTY -> ""
                        }
                        Tile(
                            label = label,
                            color = tileColor,
                            canTap = enabled && board[idx] == Cell.EMPTY,
                            size = tileSize
                        ) { onTap(idx) }
                    }
                }
                if (r < 2) Spacer(Modifier.height(gap))
            }
        }
    }
}

@Composable
private fun Tile(
    label: String,
    color: Color,
    canTap: Boolean,
    size: Dp,
    onTap: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(interaction) {
        interaction.interactions.collect { evt ->
            when (evt) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release,
                is PressInteraction.Cancel -> pressed = false
            }
        }
    }

    val scale = if (pressed) 0.98f else 1f

    val shape = RoundedCornerShape(18.dp)
    OutlinedCard(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .let {
                if (canTap) {
                    it.clickable(
                        interactionSource = interaction,
                        indication = null
                    ) { onTap() }
                } else it
            },
        shape = shape,
        border = BorderStroke(1.5.dp, Luxe.tileBorder),
        colors = CardDefaults.outlinedCardColors(
            containerColor = color
        )
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            XOIcon(label = label, boxSize = size)
        }
    }
}
@Composable
private fun XOIcon(label: String, boxSize: Dp) {
    if (label.isEmpty()) return
    Canvas(modifier = Modifier.size(boxSize)) {
        val s = size.minDimension
        val stroke = s * 0.09f
        val shadow = stroke * 1.8f
        val pad = s * 0.22f
        if (label == "X") {
            val start1 = Offset(pad, pad)
            val end1   = Offset(s - pad, s - pad)
            val start2 = Offset(pad, s - pad)
            val end2   = Offset(s - pad, pad)

            drawLine(
                color = Color.Black.copy(alpha = 0.25f),
                start = start1, end = end1,
                strokeWidth = shadow, cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.25f),
                start = start2, end = end2,
                strokeWidth = shadow, cap = StrokeCap.Round
            )

            drawLine(
                color = Luxe.xColor,
                start = start1, end = end1,
                strokeWidth = stroke, cap = StrokeCap.Round
            )
            drawLine(
                color = Luxe.xColor,
                start = start2, end = end2,
                strokeWidth = stroke, cap = StrokeCap.Round
            )
        }
        else {

            val r = (s / 2f) - pad

            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = r + stroke * 0.4f,
                style = Stroke(width = shadow, cap = StrokeCap.Round)
            )

            drawCircle(
                color = Luxe.oColor,
                radius = r,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}
@Composable
private fun ResultBanner(
    outcome: Outcome,
    opponent: Opponent
) {
    val friendly = when {
        opponent == Opponent.AI && outcome == Outcome.X_LOSES -> "Computer wins"
        opponent == Opponent.AI && outcome == Outcome.O_LOSES -> "You win"
        outcome == Outcome.DRAW -> "Result: Draw"
        outcome == Outcome.X_LOSES -> "Result: O wins"
        outcome == Outcome.O_LOSES -> "Result: X wins"
        else -> ""
    }

    val (bg, fg) = when (friendly) {
        "Computer wins" -> Color(0x26FFFFFF) to Luxe.textPrimary
        "You win"       -> Color(0x26FFFFFF) to Luxe.textPrimary
        "Result: Draw"  -> Color(0x1AFFFFFF) to Luxe.textMuted
        else            -> Color.Transparent to Color.Unspecified
    }

    if (friendly.isNotEmpty()) {
        val shape = RoundedCornerShape(12.dp)
        Surface(
            shape = shape,
            color = bg,
            contentColor = fg,
            border = BorderStroke(1.dp, Luxe.tileBorder)
        ) {
            Text(
                text = friendly,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
