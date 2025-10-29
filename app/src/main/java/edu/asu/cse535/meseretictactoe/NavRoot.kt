package edu.asu.cse535.meseretictactoe

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class AppRoute {
    MODE_SELECT,
    MAIN,
    P2P,
    PAST
}

@Composable
fun NavRoot(
    nav: NavHostController,
    gameVm: GameViewModel = viewModel()
) {
    NavHost(
        navController = nav,
        startDestination = AppRoute.MODE_SELECT.name
    ) {
        composable(AppRoute.MODE_SELECT.name) {
            WhoGoesFirstScreen(
                nav = nav,
                current = gameVm.settings,
                onApply = { newSettings ->
                    gameVm.applySettings(newSettings)
                    when (newSettings.opponent) {
                        Opponent.HUMAN_BT -> {
                            nav.navigate(AppRoute.P2P.name)
                        }
                        else -> {
                            gameVm.reset()
                            nav.navigate(AppRoute.MAIN.name)
                        }
                    }
                }
            )
        }

        composable(AppRoute.MAIN.name) {
            GameScreen(
                nav = nav,
                vm = gameVm
            )
        }

        composable(AppRoute.P2P.name) {
            P2PGameScreen(
                nav = nav,
                gameVm = gameVm
            )
        }

        composable(AppRoute.PAST.name) {
            PastGameScreen(
                nav = nav
            )
        }
    }
}
