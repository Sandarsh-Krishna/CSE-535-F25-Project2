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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class TwoPlayerMode { LOCAL, BLUETOOTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoGoesFirstScreen(
    current: GameSettings,
    onApply: (GameSettings) -> Unit
) {
    var opponent by remember { mutableStateOf(current.opponent) }
    var difficulty by remember { mutableStateOf(current.difficulty) }
    var starter by remember { mutableStateOf(current.starter) } // used for Two Player modes
    var diffMenuExpanded by remember { mutableStateOf(false) }
    var tpMode by remember { mutableStateOf(TwoPlayerMode.LOCAL) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Mode") }) }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Play against", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquareOption("AI", opponent == Opponent.AI) { opponent = Opponent.AI }
                SquareOption("Two Players", opponent == Opponent.HUMAN_LOCAL || opponent == Opponent.HUMAN_BT) {
                    opponent = Opponent.HUMAN_LOCAL
                }
            }

            Spacer(Modifier.height(28.dp))

            if (opponent == Opponent.AI) {
                Text("Difficulty", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Box(contentAlignment = Alignment.Center) {
                    ExposedDropdownMenuBox(
                        expanded = diffMenuExpanded,
                        onExpandedChange = { diffMenuExpanded = !diffMenuExpanded }
                    ) {
                        val label = when (difficulty) {
                            Difficulty.EASY -> "Easy"
                            Difficulty.MEDIUM -> "Medium"
                            Difficulty.HARD -> "Hard"
                        }
                        OutlinedTextField(
                            value = label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select difficulty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .widthIn(min = 220.dp)
                        )
                        ExposedDropdownMenu(expanded = diffMenuExpanded, onDismissRequest = { diffMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Easy") }, onClick = { difficulty = Difficulty.EASY; diffMenuExpanded = false })
                            DropdownMenuItem(text = { Text("Medium") }, onClick = { difficulty = Difficulty.MEDIUM; diffMenuExpanded = false })
                            DropdownMenuItem(text = { Text("Hard") }, onClick = { difficulty = Difficulty.HARD; diffMenuExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("You will play as X and go first.", color = MaterialTheme.colorScheme.secondary)
            } else {
                // Two Players: choose Same Device or Bluetooth
                Text("How do you want to play?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    SquareOption("Same Device", tpMode == TwoPlayerMode.LOCAL) {
                        tpMode = TwoPlayerMode.LOCAL
                        opponent = Opponent.HUMAN_LOCAL
                    }
                    SquareOption("Bluetooth", tpMode == TwoPlayerMode.BLUETOOTH) {
                        tpMode = TwoPlayerMode.BLUETOOTH
                        opponent = Opponent.HUMAN_BT
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Who goes first?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquareOption("X", selected = starter == Player.X) { starter = Player.X }
                    SquareOption("O", selected = starter == Player.O) { starter = Player.O }
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(onClick = {
                val settings = when (opponent) {
                    Opponent.AI -> GameSettings(opponent = Opponent.AI, difficulty = difficulty, starter = Player.X)
                    Opponent.HUMAN_LOCAL -> GameSettings(opponent = Opponent.HUMAN_LOCAL, difficulty = difficulty, starter = starter)
                    Opponent.HUMAN_BT -> GameSettings(opponent = Opponent.HUMAN_BT, difficulty = difficulty, starter = starter)
                }
                onApply(settings)
            }) { Text("Start") }
        }
    }
}

/** A compact option with a square indicator that fills when selected. */
@Composable
private fun SquareOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier.clip(shape).clickable { onClick() },
        shape = shape,
        tonalElevation = if (selected) 2.dp else 0.dp,
        shadowElevation = 0.dp,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(4.dp))
            )
            Text(text)
        }
    }
}
