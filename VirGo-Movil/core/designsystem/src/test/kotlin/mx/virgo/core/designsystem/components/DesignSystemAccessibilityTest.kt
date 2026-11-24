package mx.virgo.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import mx.virgo.core.designsystem.theme.DarkColorScheme
import mx.virgo.core.designsystem.theme.LightColorScheme
import mx.virgo.core.designsystem.theme.VirGoShapes
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.designsystem.theme.VirGoTypography
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
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class DesignSystemAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun lightThemeInstallsSemanticColorsTypographyAndShapes() {
        assertInstalledTheme(darkTheme = false, expectedScheme = LightColorScheme)
    }

    @Test
    fun darkThemeInstallsSemanticColorsTypographyAndShapes() {
        assertInstalledTheme(darkTheme = true, expectedScheme = DarkColorScheme)
    }

    @Test
    fun enabledButtonsAndTextFieldHaveAtLeast48DpTargets() {
        compose.setContent {
            VirGoTheme {
                Column(modifier = Modifier.width(320.dp)) {
                    VirGoButton(onClick = {}, text = "Continuar", modifier = Modifier.testTag("primary"))
                    VirGoOutlinedButton(onClick = {}, modifier = Modifier.testTag("outlined")) {
                        Text("Cancelar")
                    }
                    VirGoTextField(value = "", onValueChange = {}, modifier = Modifier.testTag("field"))
                }
            }
        }

        listOf("primary", "outlined", "field").forEach { tag ->
            compose
                .onNodeWithTag(tag)
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertWidthIsAtLeast(48.dp)
        }
    }

    @Test
    fun primaryButtonGrowsForLongLabelAtDoubleFontScaleWithoutTextOverflow() {
        compose.setContent {
            LargeTextHarness {
                VirGoButton(onClick = {}, text = LONG_LABEL, modifier = Modifier.testTag("largeButton"))
            }
        }

        assertLargeButtonLabelFits()
    }

    @Test
    fun outlinedButtonGrowsForLongLabelAtDoubleFontScaleWithoutTextOverflow() {
        compose.setContent {
            LargeTextHarness {
                VirGoOutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().testTag("largeButton"),
                ) {
                    Text(text = LONG_LABEL, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        assertLargeButtonLabelFits()
    }

    @Test
    fun errorTextFieldExposesErrorSemanticsAndKeepsItsValueAccessible() {
        compose.setContent {
            VirGoTheme {
                VirGoTextField(
                    value = "correo-invalido",
                    onValueChange = {},
                    isError = true,
                    modifier = Modifier.testTag("errorField"),
                )
            }
        }

        compose
            .onNodeWithTag("errorField")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
        compose.onNodeWithText("correo-invalido").assertIsDisplayed()
        val error = compose.onNodeWithTag("errorField").fetchSemanticsNode().config[SemanticsProperties.Error]
        assertTrue("Error semantics must include an accessibility announcement", error.isNotBlank())
    }

    @Test
    fun validTextFieldDoesNotExposeErrorSemantics() {
        compose.setContent {
            VirGoTheme {
                VirGoTextField(
                    value = "ana@example.com",
                    onValueChange = {},
                    isError = false,
                    modifier = Modifier.testTag("validField"),
                )
            }
        }

        compose
            .onNodeWithTag("validField")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    private fun assertInstalledTheme(
        darkTheme: Boolean,
        expectedScheme: ColorScheme,
    ) {
        var actualScheme: ColorScheme? = null
        var hasExpectedTypography = false
        var hasExpectedShapes = false
        compose.setContent {
            VirGoTheme(darkTheme = darkTheme) {
                actualScheme = MaterialTheme.colorScheme
                hasExpectedTypography = MaterialTheme.typography == VirGoTypography
                hasExpectedShapes = MaterialTheme.shapes == VirGoShapes
                Text("Tema")
            }
        }

        compose.onNodeWithText("Tema").assertIsDisplayed()
        compose.runOnIdle {
            val scheme = requireNotNull(actualScheme)
            val semanticColors =
                listOf(
                    Triple("primary", expectedScheme.primary, scheme.primary),
                    Triple("onPrimary", expectedScheme.onPrimary, scheme.onPrimary),
                    Triple("primaryContainer", expectedScheme.primaryContainer, scheme.primaryContainer),
                    Triple("onPrimaryContainer", expectedScheme.onPrimaryContainer, scheme.onPrimaryContainer),
                    Triple("surface", expectedScheme.surface, scheme.surface),
                    Triple("onSurface", expectedScheme.onSurface, scheme.onSurface),
                    Triple("surfaceVariant", expectedScheme.surfaceVariant, scheme.surfaceVariant),
                    Triple("onSurfaceVariant", expectedScheme.onSurfaceVariant, scheme.onSurfaceVariant),
                    Triple("outline", expectedScheme.outline, scheme.outline),
                    Triple("error", expectedScheme.error, scheme.error),
                    Triple("background", expectedScheme.background, scheme.background),
                    Triple("onBackground", expectedScheme.onBackground, scheme.onBackground),
                )
            semanticColors.forEach { (role, expected, actual) ->
                assertEquals("Theme must install the requested non-dynamic $role", expected, actual)
            }
            assertTrue("Theme must install local-font typography", hasExpectedTypography)
            assertTrue("Theme must install shared component shapes", hasExpectedShapes)
        }
    }

    private fun assertLargeButtonLabelFits() {
        val button = compose.onNodeWithTag("largeButton")
        button.assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
        val buttonBounds = button.getUnclippedBoundsInRoot()
        assertEquals("Harness must constrain the button to 320 dp", 320f, buttonBounds.width.value, 0.5f)
        assertTrue("Long double-scale label must grow the button beyond 48 dp", buttonBounds.height > 48.dp)

        val label = compose.onNodeWithText(LONG_LABEL, useUnmergedTree = true).assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        label.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue("Text must expose its actual layout result", action(layouts))
        }
        assertEquals("Button must contain one measured text layout", 1, layouts.size)
        val layout = layouts.single()
        assertTrue("Long label must wrap in the constrained harness", layout.lineCount > 1)
        assertFalse("Double-scale button label must not visually overflow", layout.hasVisualOverflow)
        val labelBounds = label.getUnclippedBoundsInRoot()
        assertTrue("Label top must stay inside the growing button", labelBounds.top >= buttonBounds.top)
        assertTrue("Label bottom must stay inside the growing button", labelBounds.bottom <= buttonBounds.bottom)
    }

    @Composable
    private fun LargeTextHarness(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
            VirGoTheme {
                Column(modifier = Modifier.width(320.dp)) {
                    content()
                }
            }
        }
    }

    private companion object {
        const val LONG_LABEL = "Continuar con la confirmación de todos los detalles de mi solicitud"
    }
}
