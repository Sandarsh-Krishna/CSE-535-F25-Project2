package edu.asu.cse535.meseretictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val nav = rememberNavController()
                    val vm = androidx.lifecycle.viewmodel.compose.viewModel<GameViewModel>()
                    val p2pVm = androidx.lifecycle.viewmodel.compose.viewModel<P2PGameViewModel>()
                    NavRoot(nav = nav, vm = vm, p2pVm = p2pVm)
                }
            }
        }
    }
}
