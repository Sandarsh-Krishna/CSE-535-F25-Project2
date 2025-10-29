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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameScreen(nav: NavHostController) {
    val fmt = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }

    val itemsDesc = remember {
        ResultsStore.items.sortedByDescending { it.timeMillis }
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
                    TextButton(onClick = { nav.popBackStack() }) {
                        Text(
                            "Back",
                            fontWeight = FontWeight.Bold
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Past Games",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsDesc) { game ->
                        OutlinedCard {
                            Column(Modifier.padding(16.dp)) {


                                Text(
                                    fmt.format(Date(game.timeMillis)),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(Modifier.height(6.dp))

                                val isAiGame = (game.opponent == Opponent.AI)


                                Row {
                                    Text(
                                        text = if (isAiGame) "Difficulty: " else "Mode: ",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = if (isAiGame)
                                            game.difficulty.name
                                        else
                                            "With Friends",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1F2937)
                                    )
                                }

                                Spacer(Modifier.height(2.dp))


                                Row {
                                    Text(
                                        text = "Outcome: ",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )


                                    val outcomeText = if (isAiGame) {
                                        when (game.outcome) {
                                            Outcome.DRAW -> "Draw"
                                            Outcome.O_LOSES -> "You win"   // X wins, and local player is X vs AI
                                            Outcome.X_LOSES -> "AI wins"   // O wins
                                            Outcome.ONGOING -> "Ongoing"
                                        }
                                    } else {

                                        when (game.outcome) {
                                            Outcome.DRAW -> "Draw"
                                            Outcome.O_LOSES -> "X wins"
                                            Outcome.X_LOSES -> "O wins"
                                            Outcome.ONGOING -> "Ongoing"
                                        }
                                    }

                                    Text(
                                        outcomeText,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
