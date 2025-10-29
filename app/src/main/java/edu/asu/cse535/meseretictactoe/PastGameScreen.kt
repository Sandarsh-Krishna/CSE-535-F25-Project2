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
                    TextButton(onClick = { nav.popBackStack() }) { Text("Back") }
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
                    fontWeight = FontWeight.ExtraBold
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
                                Text(fmt.format(Date(game.timeMillis)))
                                Spacer(Modifier.height(6.dp))

                                val isFriendGame =
                                    game.difficulty != Difficulty.EASY &&
                                            game.difficulty != Difficulty.MEDIUM &&
                                            game.difficulty != Difficulty.HARD

                                Row {
                                    Text(
                                        if (isFriendGame) "Mode: "
                                        else "Difficulty: ",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (isFriendGame) "With Friends"
                                        else game.difficulty.name
                                    )
                                }

                                Spacer(Modifier.height(2.dp))

                                Row {
                                    Text("Outcome: ", fontWeight = FontWeight.Bold)
                                    val outcomeText = when (game.outcome) {
                                        Outcome.DRAW -> "Draw"
                                        Outcome.X_LOSES -> "O wins"
                                        Outcome.O_LOSES -> "X wins"
                                        Outcome.ONGOING -> "Ongoing"
                                    }
                                    Text(outcomeText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
