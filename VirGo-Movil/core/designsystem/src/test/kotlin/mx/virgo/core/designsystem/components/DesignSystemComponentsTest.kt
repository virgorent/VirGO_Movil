package mx.virgo.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DesignSystemComponentsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun buttonDisplaysTextAndForwardsClick() {
        var clicked = false
        compose.setContent {
            VirGoTheme {
                VirGoButton(
                    onClick = { clicked = true },
                    text = "Reservar",
                )
            }
        }

        compose
            .onNodeWithText("Reservar")
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertTrue(clicked)
    }

    @Test
    fun disabledButtonIgnoresClick() {
        var clicked = false
        compose.setContent {
            VirGoTheme {
                VirGoButton(
                    onClick = { clicked = true },
                    enabled = false,
                    text = "Reservar",
                )
            }
        }

        compose.onNodeWithText("Reservar").assertIsNotEnabled().performClick()
        assertFalse(clicked)
    }

    @Test
    fun textFieldShowsPlaceholderAndAcceptsInput() {
        var value = ""
        compose.setContent {
            VirGoTheme {
                VirGoTextField(
                    value = value,
                    onValueChange = { value = it },
                    placeholder = "Correo",
                )
            }
        }

        compose
            .onNodeWithText("Correo")
            .assertIsDisplayed()
            .performClick()
            .performTextInput("ana@mail.com")
        assertEquals("ana@mail.com", value)
    }

    @Test
    fun cardDisplaysItsContent() {
        compose.setContent {
            VirGoTheme {
                VirGoCard {
                    Text(text = "Salón disponible")
                }
            }
        }

        compose.onNodeWithText("Salón disponible").assertIsDisplayed()
    }

    @Test
    fun badgeDisplaysItsText() {
        compose.setContent {
            VirGoTheme {
                VirGoBadge(text = "Nuevo")
            }
        }

        compose.onNodeWithText("Nuevo").assertIsDisplayed()
    }
}
