package mx.virgo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mx.virgo.app.ui.guest.GuestHomeScreen
import mx.virgo.app.ui.home.HomePlaceholder
import mx.virgo.app.ui.login.LoginScreen
import mx.virgo.app.ui.recover.RecoverScreen
import mx.virgo.app.ui.register.RegisterScreen

@Composable
fun VirGoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = GuestHomeRoute,
        modifier = modifier,
    ) {
        guestHomeDestination(
            onLoginClick = { navController.navigate(LoginRoute) },
            onRegisterClick = { venueId -> navController.navigate(RegisterRoute(venueId)) },
        )
        loginDestination(
            onLoginSuccess = { navController.navigateToHome() },
            onForgotPasswordClick = { navController.navigate(RecoverRoute) },
            onRegisterClick = { navController.navigate(RegisterRoute()) },
            onBackClick = { navController.popBackStack() },
        )
        registerDestination(
            onRegisterSuccess = { navController.navigateToHome() },
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
        homeDestination()
    }
}

private fun NavHostController.navigateToHome() {
    navigate(
        route = HomeRoute,
        builder = {
            popUpTo<GuestHomeRoute> { inclusive = true }
            launchSingleTop = true
        },
    )
}

private fun NavGraphBuilder.guestHomeDestination(
    onLoginClick: () -> Unit,
    onRegisterClick: (Int?) -> Unit,
) {
    composable<GuestHomeRoute> {
        GuestHomeScreen(
            onLoginClick = onLoginClick,
            onRegisterClick = onRegisterClick,
        )
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

private fun NavGraphBuilder.homeDestination() {
    composable<HomeRoute> {
        HomePlaceholder()
    }
}
