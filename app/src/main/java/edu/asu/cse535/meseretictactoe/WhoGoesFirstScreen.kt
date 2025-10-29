package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.geometry.Offset
enum class TwoPlayerMode { LOCAL, BLUETOOTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoGoesFirstScreen(
    current: GameSettings,
    onApply: (GameSettings) -> Unit
) {
    var opponent by remember { mutableStateOf(current.opponent) }
    var difficulty by remember { mutableStateOf(current.difficulty) }
    var tpMode by remember {
        mutableStateOf(
            if (current.opponent == Opponent.HUMAN_BT)
                TwoPlayerMode.BLUETOOTH
            else
                TwoPlayerMode.LOCAL
        )
    }

    val bg = Brush.linearGradient(
        colors = listOf(Luxe.bgStart, Luxe.bgEnd),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg) // CHANGE
    )  { Scaffold(
        // CHANGE: Transparent, centered app bar
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Setup",
                        color = Luxe.textPrimary, // CHANGE
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // CHANGE
                )
            )
        },
        containerColor = Color.Transparent
    ){ pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)

        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Luxe.glass, // CHANGE
                border = BorderStroke(1.dp, Luxe.glassBorder), // CHANGE
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(0.95f)
            )
            {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Luxe.textPrimary
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Play against",
                        style = MaterialTheme.typography.titleMedium,
                        color = Luxe.textPrimary // CHANGE
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquareOption(
                            text = "AI",
                            selected = (opponent == Opponent.AI)
                        ) {
                            opponent = Opponent.AI
                        }

                        SquareOption(
                            text = "Two Players",
                            selected = (opponent == Opponent.HUMAN_LOCAL || opponent == Opponent.HUMAN_BT)
                        ) {
                            opponent =
                                if (tpMode == TwoPlayerMode.BLUETOOTH)
                                    Opponent.HUMAN_BT
                                else
                                    Opponent.HUMAN_LOCAL
                        }
                    }

                    Spacer(Modifier.height(28.dp))


                    if (opponent == Opponent.AI) {
                        Text("Difficulty", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))

                        DifficultyRowSetup(
                            current = difficulty,
                            onSelect = { difficulty = it }
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "You will play as X and go first.",
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "How do you want to play?",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            SquareOption(
                                text = "Same Device",
                                selected = (tpMode == TwoPlayerMode.LOCAL)
                            ) {
                                tpMode = TwoPlayerMode.LOCAL
                                opponent = Opponent.HUMAN_LOCAL
                            }

                            SquareOption(
                                text = "Bluetooth",
                                selected = (tpMode == TwoPlayerMode.BLUETOOTH)
                            ) {
                                tpMode = TwoPlayerMode.BLUETOOTH
                                opponent = Opponent.HUMAN_BT
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }

                    Spacer(Modifier.height(32.dp))

                    PrimaryButton(
                        label = "Start",
                        onClick = {
                            val settings = when (opponent) {
                                Opponent.AI -> GameSettings(
                                    opponent = Opponent.AI,
                                    difficulty = difficulty,
                                    starter = Player.X,
                                    localSide = Player.X
                                )

                                Opponent.HUMAN_LOCAL -> GameSettings(
                                    opponent = Opponent.HUMAN_LOCAL,
                                    difficulty = difficulty,
                                    starter = Player.X,
                                    localSide = Player.X
                                )

                                Opponent.HUMAN_BT -> {
                                    GameSettings(
                                        opponent = Opponent.HUMAN_BT,
                                        difficulty = difficulty,
                                        starter = Player.X,
                                        localSide = Player.X
                                    )
                                }
                            }

                            onApply(settings)
                        }
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun DifficultyRowSetup(
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
                    Brush.horizontalGradient(listOf(Luxe.accent1, Luxe.accent2)), // CHANGE
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
private fun SelectableChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = MaterialTheme.colorScheme.primary
    val bgColors = if (active) {

        Brush.horizontalGradient(
            listOf(Luxe.accent1.copy(alpha = 0.9f), Luxe.accent2.copy(alpha = 0.9f))
        )
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val borderCol = if (active) Luxe.accent2 else Luxe.chipOutline
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = shape,
        border = BorderStroke(1.dp, borderCol),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(bgColors, shape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (active) Color.White else Luxe.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SquareOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) Luxe.accent2 else Luxe.chipOutline
    val bgBrush = if (selected) {
        Brush.verticalGradient(
            listOf(
                Luxe.tileFill.copy(alpha = 0.6f),
                Luxe.tileFill.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = shape,
        border = BorderStroke(1.dp, borderColor),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val indicatorColor = if (selected) Luxe.accent2 else Color.Transparent
            val indicatorBorderColor = if (selected) Luxe.accent2 else Luxe.chipOutline

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorColor,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(1.dp, indicatorBorderColor),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = Luxe.textPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
