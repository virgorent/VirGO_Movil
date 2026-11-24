package mx.virgo.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mx.virgo.app.BuildConfig
import mx.virgo.app.R
import mx.virgo.app.demo.DemoRole
import mx.virgo.app.demo.RoleWorkspace
import mx.virgo.app.demo.VisualDemoApp
import mx.virgo.app.session.ActiveSession
import mx.virgo.app.session.SessionRole
import mx.virgo.app.ui.guest.GuestHomeScreen
import mx.virgo.app.ui.login.LoginScreen
import mx.virgo.app.ui.recover.RecoverScreen
import mx.virgo.app.ui.register.RegisterScreen

@Composable
fun VirGoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    session: ActiveSession? = null,
    onSessionRefresh: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    LaunchedEffect(session) {
        val currentRoute = navController.currentDestination?.route
        when {
            session != null && currentRoute != HomeRoute::class.qualifiedName -> navController.navigateToHome()
            session == null && currentRoute == HomeRoute::class.qualifiedName ->
                navController.navigate(GuestHomeRoute) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
        }
    }
    NavHost(
        navController = navController,
        startDestination = if (session != null) HomeRoute else GuestHomeRoute,
        modifier = modifier,
    ) {
        guestHomeDestination(
            onLoginClick = { navController.navigate(LoginRoute) },
            onRegisterClick = { venueId -> navController.navigate(RegisterRoute(venueId)) },
            onPreviewClick = { navController.navigate(VisualPreviewRoute) },
        )
        loginDestination(
            onLoginSuccess = onSessionRefresh,
            onForgotPasswordClick = { navController.navigate(RecoverRoute) },
            onRegisterClick = { navController.navigate(RegisterRoute()) },
            onBackClick = { navController.popBackStack() },
        )
        registerDestination(
            onRegisterSuccess = {
                navController.navigate(LoginRoute) {
                    popUpTo<RegisterRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onLoginClick = {
                navController.navigate(LoginRoute) {
                    popUpTo<RegisterRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBackClick = { navController.popBackStack() },
        )
        recoverDestination(
            onDone = { navController.popBackStack(route = LoginRoute, inclusive = false) },
            onBackClick = { navController.popBackStack() },
        )
        homeDestination(session, onLogout)
        visualPreviewDestination(navController)
        developmentDestinations(navController)
    }
}

private fun NavHostController.navigateToHome() {
    navigate(
        route = HomeRoute,
        builder = {
            popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
        },
    )
}

private fun NavGraphBuilder.guestHomeDestination(
    onLoginClick: () -> Unit,
    onRegisterClick: (Int?) -> Unit,
    onPreviewClick: () -> Unit,
) {
    composable<GuestHomeRoute> {
        Column(Modifier.fillMaxSize()) {
            if (BuildConfig.DEBUG) {
                TextButton(onClick = onPreviewClick) {
                    Text(stringResource(R.string.unified_preview))
                }
            }
            GuestHomeScreen(
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun NavGraphBuilder.loginDestination(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable<LoginRoute> {
        LoginScreen(
            onLoginClick = { _, _ -> onLoginSuccess() },
            onForgotPasswordClick = onForgotPasswordClick,
            onRegisterClick = onRegisterClick,
            onBackClick = onBackClick,
        )
    }
}

private fun NavGraphBuilder.registerDestination(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable<RegisterRoute> { entry ->
        RegisterScreen(
            venueId = entry.toRoute<RegisterRoute>().venueId,
            onRegisterSuccess = onRegisterSuccess,
            onLoginClick = onLoginClick,
            onBackClick = onBackClick,
        )
    }
}

private fun NavGraphBuilder.recoverDestination(
    onDone: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable<RecoverRoute> {
        RecoverScreen(
            onDone = onDone,
            onBackClick = onBackClick,
        )
    }
}

private fun NavGraphBuilder.homeDestination(
    session: ActiveSession?,
    onLogout: () -> Unit,
) {
    composable<HomeRoute> {
        if (session != null) {
            RoleWorkspace(
                role = session.role.toWorkspaceRole(),
                accountKey = session.accountId,
                onLogout = onLogout,
                accountName = session.name,
                accountEmail = session.email,
            )
        }
    }
}

private fun SessionRole.toWorkspaceRole(): DemoRole =
    when (this) {
        SessionRole.CLIENT -> DemoRole.CLIENT
        SessionRole.OWNER -> DemoRole.OWNER
        SessionRole.PROVIDER -> DemoRole.PROVIDER
    }

private fun NavGraphBuilder.visualPreviewDestination(navController: NavHostController) {
    composable<VisualPreviewRoute> {
        if (BuildConfig.DEBUG) {
            VisualDemoApp(onExit = { navController.popBackStack() })
        } else {
            LaunchedEffect(Unit) { navController.popBackStack() }
        }
    }
}
