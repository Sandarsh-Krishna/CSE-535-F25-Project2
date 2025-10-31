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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.width
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameScreen(nav: NavHostController) {
    val fmt = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }

    val itemsDesc = remember {
        ResultsStore.items.sortedByDescending { it.timeMillis }
    }

    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF141A3A),
            Color(0xFF3B1A78)
        )
    )
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) {
                        Text(
                            "Back",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Past Games",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))
                if (itemsDesc.isEmpty()) {

                    Text(
                        text = "No games played yet.",
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = listState,
                ) {
                    items(itemsDesc) { game ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.03f)
                            ),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp
                        ) {

                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {


                                    Text(
                                        fmt.format(Date(game.timeMillis)),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF8FAFC)
                                    )
                                    Spacer(Modifier.height(6.dp))

                                    val isAiGame = (game.opponent == Opponent.AI)


                                    Row {
                                        Text(
                                            text = if (isAiGame) "Difficulty: " else "Mode: ",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Text(
                                            text = if (isAiGame)
                                                game.difficulty.name
                                            else
                                                "With Friends",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(Modifier.height(2.dp))


                                    Row {
                                        Text(
                                            text = "Outcome: ",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF8FAFC)
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

                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Outcome:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE2E8F0)
                                            )


                                            OutcomePillCompact(outcomeText)
                                        }
                                    }
                                }
                            }


                        }
                        }
                    }


            }
//        ScrollBar(
//            modifier = Modifier
//                .align(Alignment.CenterEnd)
//                .padding(end = 3.dp),
//            listState = listState
//        )
        }

    }
}

@Composable
private fun OutcomePillCompact(text: String) {
    val (bg, stroke) = when {
        text.contains("you win", ignoreCase = true) ||
                text.contains("x wins", ignoreCase = true) ->
            Color(0xFF22C55E).copy(alpha = 0.15f) to Color(0xFF22C55E)
        text.contains("ai wins", ignoreCase = true) ||
                text.contains("o wins", ignoreCase = true) ->
            Color(0xFFEF4444).copy(alpha = 0.15f) to Color(0xFFEF4444)
        else ->
            Color(0xFFF97316).copy(alpha = 0.15f) to Color(0xFFF97316)
    }

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, stroke.copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .background(bg, androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
//
//@Composable
//private fun ScrollBar(
//    modifier: Modifier = Modifier,
//    listState: androidx.compose.foundation.lazy.LazyListState
//) {
//    // how many items can appear at once
//    val totalItems = listState.layoutInfo.totalItemsCount
//    if (totalItems <= 0) return
//
//    val firstVisible = listState.firstVisibleItemIndex
//    val visibleItems = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
//
//    // progress from 0f..1f
//    val scrollProgress =
//        (firstVisible.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)).coerceIn(0f, 1f)
//
//    Box(
//        modifier = modifier
//            .fillMaxHeight()
//            .width(4.dp)
//            .background(Color.Transparent)
//    ) {
//        // track
//        Box(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .fillMaxHeight(0.85f)
//                .width(4.dp)
//                .background(Color.White.copy(alpha = 0.04f)),
//        )
//
//        // thumb
//        val thumbHeightFraction = (visibleItems.toFloat() / totalItems.toFloat())
//            .coerceIn(0.15f, 0.45f) // min/max size
//
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(top = (scrollProgress * 0.85f * 300f).dp) //
//                .width(4.dp)
//                .height((thumbHeightFraction * 0.85f * 300f).dp)
//                .background(Color.White.copy(alpha = 0.35f), shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
//        )
//    }
//}