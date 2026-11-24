package mx.virgo.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import mx.virgo.app.dev.DevMenu
import mx.virgo.app.navigation.VirGoNavHost
import mx.virgo.app.session.SessionViewModel
import mx.virgo.core.designsystem.theme.VirGoTheme

val LocalSnackbarHost = staticCompositionLocalOf { SnackbarHostState() }
val LocalDevMenu = staticCompositionLocalOf<@Composable () -> Unit> { {} }

@Composable
fun VirGoApp() {
    VirGoTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val sessionViewModel: SessionViewModel = hiltViewModel()
        val sessionState by sessionViewModel.state.collectAsState()
        val sessionError = stringResource(R.string.session_error)
        LaunchedEffect(sessionState.error, sessionState.loading) {
            if (!sessionState.loading && sessionState.error) {
                snackbarHostState.showSnackbar(sessionError)
            }
        }
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
            LocalDevMenu provides { DevMenu(navController = navController) },
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            ) { innerPadding ->
                if (sessionState.loading) {
                    SessionLoading(Modifier.fillMaxSize().padding(innerPadding))
                } else {
                    VirGoNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        session = sessionState.session,
                        onSessionRefresh = sessionViewModel::refresh,
                        onLogout = sessionViewModel::logout,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.session_loading), style = MaterialTheme.typography.bodyLarge)
    }
}
