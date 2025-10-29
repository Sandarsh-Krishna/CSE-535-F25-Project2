package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavHostController

enum class TwoPlayerMode { LOCAL, BLUETOOTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoGoesFirstScreen(
    nav: NavHostController,
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
        0f to Color(0xFFFEF3C7),
        0.5f to Color(0xFFE9D5FF),
        1f to Color(0xFFBAE6FD)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(
                        onClick = {
                            nav.popBackStack()
                        }
                    ) {
                        Text(
                            "Back",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
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
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Mode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Play against",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
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
                    ModeToggleBox(
                        text = "AI",
                        selected = (opponent == Opponent.AI),
                        onClick = {
                            opponent = Opponent.AI
                        }
                    )

                    ModeToggleBox(
                        text = "Two Players",
                        selected = (opponent == Opponent.HUMAN_LOCAL || opponent == Opponent.HUMAN_BT),
                        onClick = {
                            opponent =
                                if (tpMode == TwoPlayerMode.BLUETOOTH)
                                    Opponent.HUMAN_BT
                                else
                                    Opponent.HUMAN_LOCAL
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (opponent == Opponent.AI) {

                    Text(
                        "Difficulty",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1F2937),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    DifficultyRowSetupNoRipple(
                        current = difficulty,
                        onSelect = { difficulty = it }
                    )


                    Spacer(Modifier.height(32.dp))

                } else {
                    Text(
                        "How do you want to play?",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1F2937),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        TwoPlayerModeBox(
                            text = "Same Device",
                            selected = (tpMode == TwoPlayerMode.LOCAL),
                            onClick = {
                                tpMode = TwoPlayerMode.LOCAL
                                opponent = Opponent.HUMAN_LOCAL
                            }
                        )

                        TwoPlayerModeBox(
                            text = "Bluetooth",
                            selected = (tpMode == TwoPlayerMode.BLUETOOTH),
                            onClick = {
                                tpMode = TwoPlayerMode.BLUETOOTH
                                opponent = Opponent.HUMAN_BT
                            }
                        )
                    }


                    Spacer(Modifier.height(32.dp))
                }

                PrimaryButtonNoRipple(
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

                            Opponent.HUMAN_BT -> GameSettings(
                                opponent = Opponent.HUMAN_BT,
                                difficulty = difficulty,
                                starter = Player.X,
                                localSide = Player.X
                            )
                        }

                        onApply(settings)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeToggleBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val borderColor = Color(0xFF4C1D95)

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFDFDFE),
            Color(0xFFE5E7EB)
        )
    )

    val indicatorFill =
        if (selected) Color(0xFF4F46E5)
        else Color.Transparent
    val indicatorBorder =
        if (selected) Color.White
        else borderColor

    Box(
        modifier = Modifier
            .border(
                BorderStroke(1.dp, borderColor),
                shape
            )
            .background(bgBrush, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorFill,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            indicatorBorder
                        ),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DifficultyRowSetupNoRipple(
    current: Difficulty,
    onSelect: (Difficulty) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DifficultyChipNoRipple(
            label = "Easy",
            active = current == Difficulty.EASY,
            onClick = { onSelect(Difficulty.EASY) }
        )
        DifficultyChipNoRipple(
            label = "Medium",
            active = current == Difficulty.MEDIUM,
            onClick = { onSelect(Difficulty.MEDIUM) }
        )
        DifficultyChipNoRipple(
            label = "Hard",
            active = current == Difficulty.HARD,
            onClick = { onSelect(Difficulty.HARD) }
        )
    }
}

@Composable
private fun DifficultyChipNoRipple(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = Color(0xFF4C1D95)

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFDFDFE),
            Color(0xFFE5E7EB)
        )
    )

    val indicatorFill =
        if (active) Color(0xFF4F46E5)
        else Color.Transparent
    val indicatorBorder =
        if (active) Color.White
        else borderColor

    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, borderColor), shape)
        /* background stays light either way to match your other chips */,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush, shape)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorFill,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(1.dp, indicatorBorder),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text = label,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TwoPlayerModeBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = Color(0xFF4C1D95)

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFFFDFDFE),
            Color(0xFFE5E7EB)
        )
    )

    val indicatorFill =
        if (selected) Color(0xFF4F46E5)
        else Color.Transparent
    val indicatorBorder =
        if (selected) Color.White
        else borderColor

    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, borderColor), shape)
            .background(bgBrush, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorFill,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(1.dp, indicatorBorder),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PrimaryButtonNoRipple(
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val btnBg = Brush.verticalGradient(
        listOf(
            Color(0xFF4F46E5),
            Color(0xFF4338CA)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 140.dp)
                .border(
                    BorderStroke(1.dp, Color(0xFF4C1D95)),
                    shape
                )
                .background(btnBg, shape)
                .padding(horizontal = 28.dp, vertical = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
