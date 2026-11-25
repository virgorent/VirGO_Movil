package mx.virgo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mx.virgo.app.R
import mx.virgo.app.demo.DemoRole
import mx.virgo.app.demo.RoleWorkspace
import mx.virgo.app.session.ActiveSession
import mx.virgo.app.session.SessionRole
import mx.virgo.app.ui.beta.BetaCatalogScreen
import mx.virgo.app.ui.beta.BetaUnavailableScreen
import mx.virgo.app.ui.login.LoginScreen
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
            onRegisterClick = { navController.navigate(RegisterRoute()) },
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
            onBackClick = { navController.popBackStack() },
        )
        homeDestination(session, onLogout)
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
    onRegisterClick: () -> Unit,
) {
    composable<GuestHomeRoute> {
        BetaCatalogScreen(onLogin = onLoginClick, onRegister = onRegisterClick)
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

private fun NavGraphBuilder.recoverDestination(onBackClick: () -> Unit) {
    composable<RecoverRoute> {
        BetaUnavailableScreen(title = R.string.beta_recovery, onBack = onBackClick)
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
