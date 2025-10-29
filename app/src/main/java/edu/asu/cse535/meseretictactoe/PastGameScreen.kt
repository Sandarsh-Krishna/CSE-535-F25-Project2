package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameScreen(nav: NavHostController) {
    val fmt = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }
    val bg = Brush.linearGradient(
        colors = listOf(Luxe.bgStart, Luxe.bgEnd),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f)
    )

    val sortedItems = remember(ResultsStore.items) {
        ResultsStore.items.sortedByDescending { it.timeMillis }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Past Games", color = Luxe.textPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    TextButton(onClick = { nav.goBack() }) {
                        Text("Back", color = Luxe.textPrimary)
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
                .padding(pad)

        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Luxe.glass,
                border = BorderStroke(1.dp, Luxe.glassBorder),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxSize()
            ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sortedItems) { game ->
                        val modeOrDifficulty = if (game.difficulty == Difficulty.EASY) {
                            "Mode: With Friends"
                        } else {
                            "Difficulty: ${game.difficulty.name}"
                        }

                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Luxe.tileBorder),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Luxe.tileFill
                            ),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    fmt.format(Date(game.timeMillis)),
                                    color = Luxe.textPrimary, // CHANGE
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(6.dp))

                                Row {
                                    Text(
                                        modeOrDifficulty.substringBefore(":") + ": ",
                                        fontWeight = FontWeight.Bold,
                                        color = Luxe.textPrimary // CHANGE
                                    )
                                    Text(
                                        modeOrDifficulty.substringAfter(": ")
                                            .ifEmpty { "With Friends" },
                                        color = Luxe.textPrimary // CHANGE
                                    )
                                }
                                Spacer(Modifier.height(4.dp))

                                Row {
                                    Text(
                                        "Outcome: ",
                                        fontWeight = FontWeight.Bold,
                                        color = Luxe.textPrimary // CHANGE
                                    )
                                    val outcomeText = when (game.outcome) {
                                        Outcome.DRAW -> "Draw"
                                        Outcome.X_LOSES -> "O wins"
                                        Outcome.O_LOSES -> "X wins"
                                        Outcome.ONGOING -> "Ongoing"
                                    }
                                    Text(outcomeText, color = Luxe.textPrimary)
                                }
                            }
                        }
                            }
                        }

                    }
                }
            }
        }
    }

}
