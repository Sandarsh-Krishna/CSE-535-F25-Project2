package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ElevatedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel, nav: NavHostController) {
    val ui by vm.ui.collectAsState()
    val settings = vm.settings
    val aiSide = vm.aiSide

    val bg = Brush.linearGradient(
        0f to Color(0xFFFEF3C7),
        0.5f to Color(0xFFE9D5FF),
        1f to Color(0xFFBAE6FD)
    )

    Scaffold(
        topBar = {
            // Buttons only; title is centered in the content below
            TopAppBar(
                title = {},
                navigationIcon = { TextButton(onClick = { nav.goBack() }) { Text("Back") } },
                actions = {
                    TextButton(onClick = { nav.navigate(AppRoute.PAST.name) }) { Text("Past") }
                    TextButton(onClick = { nav.navigate(AppRoute.MODE_SELECT.name) }) { Text("Mode") }
                }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered title BELOW the app bar
                    Text(
                        "Misère Tic-Tac-Toe",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))
                    ModePill(settings)

                    Spacer(Modifier.height(12.dp))
                    TurnChip(player = ui.state.playerToMove)

                    Spacer(Modifier.height(16.dp))

                    Board(
                        board = ui.state.board,
                        enabled = ui.outcome == Outcome.ONGOING &&
                                (aiSide == null || ui.state.playerToMove != aiSide)
                    ) { vm.tap(it) }

                    Spacer(Modifier.height(16.dp))

                    if (ui.outcome != Outcome.ONGOING) {
                        ResultBanner(ui.outcome, settings.opponent)
                        Spacer(Modifier.height(12.dp))
                    }

                    ElevatedButton(
                        onClick = { vm.reset() },
                        shape = RoundedCornerShape(16.dp) // rounded rectangle
                    ) { Text("New Game") }
                }
            }
        }
    }
}

@Composable
private fun ModePill(settings: GameSettings) {
    val label = when (settings.opponent) {
        Opponent.AI -> "AI • ${settings.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} • You’re X"
        Opponent.HUMAN_LOCAL -> "Two Players • Same Device"
        Opponent.HUMAN_BT -> "Two Players • Bluetooth"
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFDCFCE7),
        contentColor = Color(0xFF065F46),
        border = BorderStroke(1.dp, Color(0xFF10B981))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TurnChip(player: Player) {
    val (bg, fg) = if (player == Player.X)
        Color(0xFFFFEDD5) to Color(0xFF9A3412)
    else
        Color(0xFFE0E7FF) to Color(0xFF3730A3)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        contentColor = fg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.25f))
    ) {
        Text(
            text = "Turn: $player",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

/** Identical color and **equal-sized** tiles using available width. */
@Composable
private fun Board(board: List<Cell>, enabled: Boolean, onTap: (Int) -> Unit) {
    val tileColor = Color(0xFFF5F3FF) // single pastel for every tile
    val gap = 12.dp

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 3 tiles per row -> 2 gaps between tiles
        val tileSize: Dp = (maxWidth - gap * 2) / 3

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                ) {
                    for (c in 0 until 3) {
                        val idx = r * 3 + c
                        val label = when (board[idx]) { Cell.X -> "X"; Cell.O -> "O"; Cell.EMPTY -> "" }
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
private fun Tile(label: String, color: Color, canTap: Boolean, size: Dp, onTap: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { evt ->
            when (evt) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
            }
        }
    }
    val scale = if (pressed) 0.98f else 1f

    OutlinedCard(
        modifier = Modifier
            .size(size) // <- equal size from layout calculation
            .scale(scale)
            .let { if (canTap) it.clickable(interactionSource = interaction, indication = null) { onTap() } else it },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.8f)),
        colors = CardDefaults.outlinedCardColors(containerColor = color.copy(alpha = 0.95f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val txtColor = if (label == "X") Color(0xFFEF4444) else Color(0xFF3B82F6)
            Text(
                text = label,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = txtColor
            )
        }
    }
}

@Composable
private fun ResultBanner(outcome: Outcome, opponent: Opponent) {
    // Map AI results to friendly text
    val friendly = when {
        opponent == Opponent.AI && outcome == Outcome.X_LOSES -> "Computer wins"
        opponent == Opponent.AI && outcome == Outcome.O_LOSES -> "You win"
        outcome == Outcome.DRAW -> "Result: Draw"
        outcome == Outcome.X_LOSES -> "Result: X loses"
        outcome == Outcome.O_LOSES -> "Result: O loses"
        else -> ""
    }

    val (bg, fg) = when (friendly) {
        "Computer wins" -> Color(0xFFFFE4E6) to Color(0xFF991B1B)
        "You win"       -> Color(0xFFE0EAFF) to Color(0xFF1D4ED8)
        "Result: Draw"  -> Color(0xFFE5E7EB) to Color(0xFF111827)
        else            -> Color.Transparent to Color.Unspecified
    }

    if (friendly.isNotEmpty()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bg,
            contentColor = fg,
            border = if (bg != Color.Transparent) BorderStroke(1.dp, fg.copy(alpha = 0.25f)) else null
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
