package mx.virgo.app.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
                )
            }
        }
        compose.onNodeWithText("Cambiar rol").assertDoesNotExist()
        compose.onNodeWithText("Cerrar sesión").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(loggedOut) }
    }

    @Test
    fun ownerIndexCannotOfferProviderAdministration() {
        compose.setContent {
            VirGoTheme {
                RoleWorkspace(DemoRole.OWNER, "synthetic-account-owner", onLogout = {})
            }
        }
        compose.onNodeWithText("Todas las pantallas de este rol").performClick()
        compose.onNodeWithText("Mis salones").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Mi servicio").assertDoesNotExist()
        compose.onNodeWithText("Zonas de cobertura").assertDoesNotExist()
    }
}
