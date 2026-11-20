package mx.virgo.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import mx.virgo.app.dev.DevMenu
import mx.virgo.app.navigation.VirGoNavHost
import mx.virgo.core.designsystem.theme.VirGoTheme

val LocalSnackbarHost = staticCompositionLocalOf { SnackbarHostState() }
val LocalDevMenu = staticCompositionLocalOf<@Composable () -> Unit> { {} }

@Composable
fun VirGoApp() {
    VirGoTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
            LocalDevMenu provides { DevMenu(navController = navController) },
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            ) { innerPadding ->
                VirGoNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
