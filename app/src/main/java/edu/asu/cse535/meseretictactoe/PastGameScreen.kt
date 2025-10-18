package edu.asu.cse535.meseretictactoe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameScreen(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Past Games") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Back") }
                }
            )
        }
    ) { pad ->
        val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (ResultsStore.items.isEmpty()) {
                Text("No games yet.")
            } else {
                ResultsStore.items.forEach { g ->
                    ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(fmt.format(Date(g.timeMillis)))
                            Spacer(Modifier.height(4.dp))
                            Text("Difficulty: ${g.difficulty.name}")
                            Text("Outcome: " + when (g.outcome) {
                                Outcome.DRAW -> "Draw"
                                Outcome.X_LOSES -> "X loses"
                                Outcome.O_LOSES -> "O loses"
                                Outcome.ONGOING -> "Ongoing"
                            })
                        }
                    }
                }
            }
        }
    }
}
