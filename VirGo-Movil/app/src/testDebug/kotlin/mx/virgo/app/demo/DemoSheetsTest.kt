package mx.virgo.app.demo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.VirGoTheme
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
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class DemoSheetsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun allFifteenSheetsOpenAndHaveReachableCloseAction() {
        var role by mutableStateOf(DemoRole.CLIENT)
        var route by mutableStateOf("explore")
        compose.setContent { VirGoTheme { DemoSheetActions(role, route, {}) } }
        demoSheetDefinitions.forEach { sheet ->
            println("Checking demo sheet ${sheet.id}")
            compose.runOnIdle {
                role = sheet.roles.first()
                route = sheet.route
            }
            compose.waitForIdle()
            compose.onAllNodesWithText(text(sheet.title))[0].performClick()
            compose.waitForIdle()
            compose
                .onNodeWithText(text(R.string.demo_sheet_close))
                .performScrollTo()
            compose.waitForIdle()
            compose
                .onNodeWithText(text(R.string.demo_sheet_close))
                .assertIsDisplayed()
                .performClick()
            compose.waitForIdle()
        }
    }

    @Test
    fun monetaryFieldsRejectZeroNegativeNonFiniteAndOverLimitAmounts() {
        assertTrue(demoSheetAmountValid("12500"))
        listOf("", "0", "-10", "NaN", "Infinity", "1000001", "invalid").forEach {
            assertFalse(it, demoSheetAmountValid(it))
        }
    }

    private fun text(res: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(res)
}
