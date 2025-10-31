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
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.TopAppBarDefaults
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

    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF141A3A),
            Color(0xFF3B1A78)
        )
    )

    Scaffold(
        modifier = Modifier.background(bg),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(
                        onClick = {
                            nav.popBackStack()
                        }
                    ) {
                        
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    navigationIconContentColor = Color.White
                )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg
                        )
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                )
                {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Text(
                            text = "Misère Tic-Tac-Toe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Select Mode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF8FAFC),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Play against",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0),
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
                                color = Color(0xFFE2E8F0),
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
                                color = Color(0xFFE2E8F0),
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
    }
}

@Composable
private fun ModeToggleBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)


    val bgBrush = Brush.horizontalGradient(
        listOf(
            Color(0xFF7C3AED),
            Color(0xFF6366F1)
        )
    )

//    val indicatorFill =
//        if (selected) Color(0xFF4F46E5)
//        else Color.Transparent
//    val indicatorBorder =
//        if (selected) Color.White
//        else borderColor

    Box(
        modifier = Modifier

            .background(bgBrush, shape)
            .then(
                if (selected) {
                    Modifier.border(
                        BorderStroke(1.2.dp, Color.White.copy(alpha = 0.6f)),
                        shape
                    )
                } else {
                    Modifier.border(
                        BorderStroke(1.2.dp, Color(0xFF94A3B8).copy(alpha = 0.4f)),
                        shape
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            ,
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
                        if (selected) Color.White else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (selected) Color.White else Color(0xFF94A3B8).copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(6.dp)
                    )
            )


            Text(
                text,
                color = if (selected) Color.White else Color(0xFFE2E8F0),
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
    val shape = RoundedCornerShape(10.dp)
    val borderColor = Color(0xFF4C1D95)

//    val bgBrush = Brush.verticalGradient(
//        listOf(
//            Color(0xFFFDFDFE),
//            Color(0xFFE5E7EB)
//        )
//    )
//
//    val indicatorFill =
//        if (active) Color(0xFF4F46E5)
//        else Color.Transparent
//    val indicatorBorder =
//        if (active) Color.White
//        else borderColor

    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, borderColor), shape)
        /* background stays light either way to match your other chips */,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (active) Color(0xFF6366F1).copy(alpha = 0.18f) else Color(0xFF0F172A).copy(alpha = 0.25f),
                    shape
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (active) Color(0xFF6366F1) else Color(0xFF94A3B8).copy(alpha = 0.3f)
                    ),
                    shape
                )
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
                        if (active) Color(0xFF6366F1) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (active) Color.White else Color(0xFF94A3B8).copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text = label,
                color = if (active) Color.White else Color(0xFFE2E8F0),
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
    val shape = RoundedCornerShape(12.dp)
    //val borderColor = Color(0xFF4C1D95)

//    val bgBrush = Brush.verticalGradient(
//        listOf(
//            Color(0xFFFDFDFE),
//            Color(0xFFE5E7EB)
//        )
//    )

//    val indicatorFill =
//        if (selected) Color(0xFF4F46E5)
//        else Color.Transparent
//    val indicatorBorder =
//        if (selected) Color.White
//        else borderColor

    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF22C55E).copy(alpha = 0.16f) else Color(0xFF0F172A).copy(alpha = 0.25f),
                shape
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) Color(0xFF22C55E) else Color(0xFF94A3B8).copy(alpha = 0.3f)
                ),
                shape
            )
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
                        if (selected) Color(0xFF22C55E) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (selected) Color.White else Color(0xFF94A3B8).copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = if (selected) Color.White else Color(0xFFE2E8F0),
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
    val shape = RoundedCornerShape(16.dp)
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
