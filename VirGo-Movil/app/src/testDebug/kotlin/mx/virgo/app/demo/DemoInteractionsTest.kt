package mx.virgo.app.demo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class DemoInteractionsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun successfulPaymentExampleLeadsToExplicitlyInvalidReceipt() {
        var route by mutableStateOf("payment")
        compose.setContent {
            VirGoTheme { ClientDemoScreen(route, onNavigate = { route = it }, onNotice = {}) }
        }
        click(R.string.demo_client_simulate_success)
        compose.runOnIdle { assertEquals("receipt", route) }
        compose.onNodeWithText(text(R.string.demo_client_receipt_not_valid)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.demo_client_paid_example)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun paymentFailureRemainsInDemoAndShowsRetryableState() {
        var route = "payment"
        compose.setContent {
            VirGoTheme { ClientDemoScreen("payment", onNavigate = { route = it }, onNotice = {}) }
        }
        click(R.string.demo_client_simulate_failure)
        compose.onNodeWithText(text(R.string.demo_client_payment_failed)).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals("payment", route) }
    }

    @Test
    fun fieldAcceptsLimitButRejectsLimitPlusOneAnd1500Characters() {
        var value by mutableStateOf("")
        val label = text(R.string.demo_name_label)
        compose.setContent {
            VirGoTheme { DemoField(label, value, onValueChange = { value = it }) }
        }
        compose.onNodeWithText(label).performTextInput("a".repeat(FIELD_LIMIT))
        compose.runOnIdle { assertEquals(FIELD_LIMIT, value.length) }
        compose.onNodeWithText(label).performTextInput("b")
        compose.runOnIdle { assertEquals(FIELD_LIMIT, value.length) }
        compose.runOnIdle { value = "" }
        compose.onNodeWithText(label).performTextInput("a".repeat(OVERSIZED_INPUT))
        compose.runOnIdle { assertEquals("", value) }
    }

    @Test
    fun chatMessageIsExplicitlyLocal() {
        val message = "Ejemplo de mensaje local"
        compose.setContent { VirGoTheme { SharedDemoScreen(DemoRole.CLIENT, "chat", {}, {}) } }
        compose.onNodeWithText(text(R.string.demo_chat_input)).performScrollTo().performTextInput(message)
        click(R.string.demo_chat_send)
        compose.onNodeWithText(message).performScrollTo().assertTextContains(message)
        compose.onNodeWithText(text(R.string.demo_chat_local)).assertIsDisplayed()
    }

    private fun click(res: Int) {
        compose.onNodeWithText(text(res)).performScrollTo().performClick()
    }

    private fun text(res: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(res)
}

private const val FIELD_LIMIT = 500
private const val OVERSIZED_INPUT = 1500
