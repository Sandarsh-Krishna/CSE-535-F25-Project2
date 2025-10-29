package edu.asu.cse535.meseretictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        ResultsStore.init(applicationContext)

        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val gameVm: GameViewModel = viewModel()

    Surface(color = MaterialTheme.colorScheme.background) {
        NavRoot(
            nav = navController,
            gameVm = gameVm
        )
    }
}
