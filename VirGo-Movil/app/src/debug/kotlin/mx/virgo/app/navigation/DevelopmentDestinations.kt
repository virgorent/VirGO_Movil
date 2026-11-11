package mx.virgo.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import mx.virgo.app.dev.DiagnosticsRoute
import mx.virgo.app.dev.DiagnosticsScreen

fun NavGraphBuilder.developmentDestinations(navController: NavHostController) {
    composable<DiagnosticsRoute> {
        DiagnosticsScreen(onBackClick = { navController.popBackStack() })
    }
}
