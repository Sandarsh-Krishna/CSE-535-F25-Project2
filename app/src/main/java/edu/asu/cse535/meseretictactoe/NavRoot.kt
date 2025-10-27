package edu.asu.cse535.meseretictactoe

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class AppRoute { MODE_SELECT, MAIN, P2P, PAST }

@Composable
fun NavRoot(
    nav: NavHostController,
    vm: GameViewModel,
    p2pVm: P2PGameViewModel
) {
    NavHost(
        navController = nav,
        startDestination = AppRoute.MODE_SELECT.name
    ) {


        composable(AppRoute.MODE_SELECT.name) {
            WhoGoesFirstScreen(
                current = vm.settings,
                onApply = { settings ->


                    when (settings.opponent) {

                        Opponent.HUMAN_BT -> {

                            nav.navigate(AppRoute.P2P.name)
                        }

                        Opponent.HUMAN_LOCAL,
                        Opponent.AI -> {

                            vm.applySettings(settings)
                            vm.reset()


                            nav.navigate(AppRoute.MAIN.name)
                        }
                    }
                }
            )
        }


        composable(AppRoute.MAIN.name) {
            GameScreen(vm = vm, nav = nav)
        }


        composable(AppRoute.PAST.name) {
            PastGameScreen(nav = nav)
        }


        composable(AppRoute.P2P.name) {

            P2PGameScreen(
                nav = nav,
                gameVm = vm,
                p2pVm = p2pVm
            )
        }
    }
}


fun NavHostController.goTo(route: AppRoute) = navigate(route.name)
fun NavHostController.goBack() = popBackStack()
