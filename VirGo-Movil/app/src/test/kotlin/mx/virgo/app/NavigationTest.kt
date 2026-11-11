package mx.virgo.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    private fun string(
        resId: Int,
        vararg args: Any,
    ): String = ApplicationProvider.getApplicationContext<Context>().getString(resId, *args)

    @Test
    fun provisionalHomeIsTheStartDestination() {
        compose.onNodeWithText(string(R.string.guest_brand)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_login)).assertIsDisplayed()
    }

    @Test
    fun venuePreviewOpensRegisterAndBackReturnsToProvisionalHome() {
        compose
            .onNode(
                hasContentDescription(value = "Fiestas", substring = true) or
                    hasText(text = "Fiestas", substring = true),
            ).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.guest_preview_see_more)).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(string(R.string.register_title)).assertIsDisplayed()

        compose.onNodeWithContentDescription(string(R.string.action_back)).performClick()
        compose.waitForIdle()

        compose.onNodeWithText(string(R.string.guest_brand)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_login)).assertIsDisplayed()
    }
}
