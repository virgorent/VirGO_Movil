package mx.virgo.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.virgo.app.dev.DevMenu
import mx.virgo.app.navigation.HomeRoute
import mx.virgo.app.navigation.VirGoNavHost
import mx.virgo.app.navigation.topLevelDestinations
import mx.virgo.core.designsystem.theme.VirGoTheme

val LocalSnackbarHost = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun VirGoApp() {
    VirGoTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val onHomeDestination = backStackEntry?.destination?.hasRoute<HomeRoute>() == true

        CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    if (onHomeDestination) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            DevMenu(navController = navController)
                        }
                    }
                },
                bottomBar = {
                    if (onHomeDestination) {
                        VirGoBottomBar(
                            onHomeClick = {
                                navController.navigate(route = HomeRoute) { launchSingleTop = true }
                            },
                        )
                    }
                },
            ) { innerPadding ->
                VirGoNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun VirGoBottomBar(
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = true,
                onClick = onHomeClick,
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
            )
        }
    }
}
