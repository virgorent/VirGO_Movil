package mx.virgo.app.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class DemoRestorationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun chosenRoleRestoresAndCanReturnToChooser() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { VirGoTheme { VisualDemoApp(onExit = {}) } }
        compose.onNodeWithText("Propietario de salón").performScrollTo().performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Cambiar rol").assertIsDisplayed().performClick()
        compose.onNodeWithText("Cliente").assertIsDisplayed()
    }
}
