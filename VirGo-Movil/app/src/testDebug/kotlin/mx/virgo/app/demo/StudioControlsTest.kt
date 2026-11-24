package mx.virgo.app.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class StudioControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun studioSectionsAreExclusiveTabsRatherThanPrimaryButtons() {
        studio()
        compose
            .onNodeWithText("Estilo")
            .performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assertIsSelected()
        compose.onNodeWithText("Mesas").performClick().assertIsSelected()
        compose.onNodeWithText("Estilo").assertIsNotSelected()
        compose.onNodeWithText("10 mesas de 10 personas", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun styleSelectorOpensOptionsAndKeepsTheChosenConcept() {
        studio()
        compose.onNodeWithText("Concepto de ejemplo").performScrollTo().performClick()
        compose.onNodeWithText("Contemporáneo violeta").performClick()
        compose.onNodeWithText("Concepto de ejemplo").performClick()
        compose
            .onNode(
                hasText("Contemporáneo violeta") and SemanticsMatcher.expectValue(SemanticsProperties.Selected, true),
            ).assertIsSelected()
        compose.onNodeWithText("Jardín romántico").assertIsDisplayed().performClick()
        compose.onNodeWithText("Mesas").performScrollTo().performClick()
        compose.onNodeWithText("Estilo").performClick()
        compose.onNodeWithText("Concepto de ejemplo").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun compactDarkLargeTextKeepsTabsAndActionsReachable() {
        studio(largeText = true)
        compose
            .onNodeWithText("Personal")
            .performScrollTo()
            .performClick()
            .assertIsSelected()
        compose.onNodeWithText("Meseros").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Simular guardar diseño").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun navigationRowsHaveAGapAndStillNavigate() {
        var destination = ""
        compose.setContent {
            VirGoTheme {
                DemoPage("Enlaces") {
                    DemoNavigationLink("Primero") { destination = "first" }
                    DemoNavigationLink("Segundo") { destination = "second" }
                }
            }
        }
        val first = compose.onNodeWithText("Primero").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithText("Segundo").getUnclippedBoundsInRoot()
        assertTrue(second.top - first.bottom >= 16.dp)
        compose.onNodeWithText("Segundo").performClick()
        compose.runOnIdle { assertTrue(destination == "second") }
    }

    private fun studio(largeText: Boolean = false) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, if (largeText) LARGE_FONT_SCALE else 1f),
            ) {
                VirGoTheme(darkTheme = largeText) {
                    ClientDemoStudio(onNavigate = {}, onNotice = {})
                }
            }
        }
    }
}

private const val LARGE_FONT_SCALE = 2f
