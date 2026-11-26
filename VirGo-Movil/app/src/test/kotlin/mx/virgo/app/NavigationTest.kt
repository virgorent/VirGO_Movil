package mx.virgo.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
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

    @Test
    fun betaCatalogIsTheStartDestinationWithoutDemoAccess() {
        compose.onNodeWithText(string(R.string.beta_label)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.beta_login)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.unified_preview)).assertDoesNotExist()
        compose.onNodeWithText("Cambiar rol").assertDoesNotExist()
    }

    @Test
    fun betaCatalogOpensRegisterWithoutSelectingASampleVenue() {
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
        compose.onNodeWithText(string(R.string.beta_login)).performClick()
        compose.onNodeWithText(string(R.string.login_title)).performScrollTo().assertIsDisplayed()
    }
}
