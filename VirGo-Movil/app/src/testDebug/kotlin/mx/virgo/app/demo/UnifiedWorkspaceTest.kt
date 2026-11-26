package mx.virgo.app.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.ui.account.AccountProfileContent
import mx.virgo.app.ui.account.AccountProfileUiState
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class UnifiedWorkspaceTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun authenticatedWorkspaceDoesNotOfferRoleSwitchingAndCanLogOut() {
        var loggedOut = false
        compose.setContent {
            VirGoTheme {
                RoleWorkspace(
                    role = DemoRole.OWNER,
                    accountKey = "synthetic-account-owner",
                    onLogout = { loggedOut = true },
                    accountScreen = { identity, logout ->
                        AccountProfileContent(
                            AccountProfileUiState(loading = false),
                            identity,
                            onRetry = {},
                            onLogout = logout,
                        )
                    },
                )
            }
        }
        compose.onNodeWithText("Cambiar rol").assertDoesNotExist()
        // 035 RF-09: «Cerrar sesión» vive en Cuenta, no en la barra superior.
        compose.onNodeWithText("Cerrar sesión").assertDoesNotExist()
        compose.onNodeWithText("Perfil").performClick()
        compose
            .onNodeWithText("Cerrar sesión")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.runOnIdle { assertTrue(loggedOut) }
    }

    @Test
    fun ownerIndexCannotOfferProviderAdministration() {
        compose.setContent {
            VirGoTheme {
                RoleWorkspace(DemoRole.OWNER, "synthetic-account-owner", onLogout = {})
            }
        }
        compose.onNodeWithText("Secciones").performClick()
        compose.onNodeWithText("Mis salones").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Mi servicio").assertDoesNotExist()
        compose.onNodeWithText("Zonas de cobertura").assertDoesNotExist()
    }
}
