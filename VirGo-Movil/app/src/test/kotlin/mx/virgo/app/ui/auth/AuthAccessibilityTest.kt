package mx.virgo.app.ui.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h640dp")
class AuthAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun filledFieldKeepsItsAccessibleName() {
        val label = string(R.string.field_email)
        compose.setContent {
            VirGoTheme {
                AuthTextField(value = "ana@example.com", onValueChange = {}, label = label)
            }
        }
        compose.onNodeWithContentDescription(label).assertIsDisplayed()
        compose.onNodeWithText("ana@example.com").assertIsDisplayed()
    }

    @Test
    fun fieldExposesSpecificErrorAndClearsItWhenCorrected() {
        val invalid = mutableStateOf(true)
        val label = string(R.string.field_email)
        val error = string(R.string.error_email)
        compose.setContent {
            VirGoTheme(darkTheme = true) {
                AuthTextField(
                    value = if (invalid.value) "invalid" else "ana@example.com",
                    onValueChange = {},
                    label = label,
                    error = error,
                    showError = invalid.value,
                )
            }
        }
        compose
            .onNodeWithContentDescription(label)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, error))
        compose
            .onNodeWithText(error)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.runOnIdle { invalid.value = false }
        compose
            .onNodeWithContentDescription(label)
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
        compose.onNodeWithText("ana@example.com").assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityHas48DpTargetAndUpdatedDescription() {
        val label = string(R.string.field_password)
        compose.setContent {
            VirGoTheme {
                PasswordField(value = "test-password", onValueChange = {}, label = label)
            }
        }
        compose
            .onNodeWithContentDescription(string(R.string.field_show_password))
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
            .performClick()
        compose.onNodeWithContentDescription(string(R.string.field_hide_password)).assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription(string(R.string.field_show_password)).assertIsDisplayed()
        compose.onNodeWithContentDescription(label).assertIsDisplayed()
    }

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)
}
