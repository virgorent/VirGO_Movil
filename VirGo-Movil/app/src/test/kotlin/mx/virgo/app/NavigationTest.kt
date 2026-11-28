package mx.virgo.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class NavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    /**
     * La app real arranca restaurando la sesión y pidiendo el catálogo a la API; con la red lenta
     * los botones tardan. Se espera a que aparezcan en lugar de fallar al primer cuadro.
     */
    private fun awaitText(text: String) {
        compose.waitUntil(STARTUP_TIMEOUT_MS) {
            compose.onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun betaCatalogIsTheStartDestinationWithoutDemoAccess() {
        awaitText(string(R.string.beta_login))
        compose.onNodeWithText(string(R.string.beta_label)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.beta_login)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.unified_preview)).assertDoesNotExist()
        compose.onNodeWithText("Cambiar rol").assertDoesNotExist()
    }

    @Test
    fun betaCatalogOpensRegisterWithoutSelectingASampleVenue() {
        awaitText(string(R.string.beta_register))
        compose.onNodeWithText(string(R.string.beta_register)).performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText(string(R.string.register_title)).performScrollTo().assertIsDisplayed()

        compose
            .onNodeWithContentDescription(
                string(R.string.action_back),
            ).performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.waitForIdle()

        // 035: el encabezado es fijo, fuera del área desplazable.
        compose.onNodeWithText(string(R.string.beta_label)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.beta_login)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun betaCatalogOpensRealLogin() {
        awaitText(string(R.string.beta_login))
        compose.onNodeWithText(string(R.string.beta_login)).performClick()
        compose.onNodeWithText(string(R.string.login_title)).performScrollTo().assertIsDisplayed()
    }
}

private const val STARTUP_TIMEOUT_MS = 15_000L
