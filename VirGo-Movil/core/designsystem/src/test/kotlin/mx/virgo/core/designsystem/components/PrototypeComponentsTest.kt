package mx.virgo.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.designsystem.theme.VirGoTone
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** T3502–T3503: componentes del lenguaje del prototipo. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class PrototypeComponentsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun pageHeaderShowsKickerTitleAsHeadingAndAccessibleBack() {
        var back = false
        compose.setContent {
            VirGoTheme { VirGoPageHeader(title = "Tu cuenta", kicker = "Cliente", onBack = { back = true }) }
        }
        // El kicker se dibuja en mayúsculas, pero su semántica conserva el texto original.
        compose.onNodeWithText("Cliente").assertIsDisplayed()
        compose.onNodeWithText("Tu cuenta").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose
            .onNode(hasClickAction() and hasContentDescription("Regresar"))
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
            .performClick()
        compose.runOnIdle { assertTrue(back) }
    }

    @Test
    fun clickableRowIsAtLeast48DpAndForwardsClick() {
        var opened = false
        compose.setContent {
            VirGoTheme {
                VirGoRow(title = "Tu cuenta", subtitle = "Datos", icon = Icons.Outlined.Person, onClick = {
                    opened =
                        true
                })
            }
        }
        compose
            .onNode(hasClickAction() and hasText("Tu cuenta", substring = true))
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        compose.runOnIdle { assertTrue(opened) }
    }

    @Test
    fun staticRowHasNoClickAction() {
        compose.setContent {
            VirGoTheme {
                VirGoRow(title = "Mensajes", trailing = { VirGoBadge(text = "Próximamente", tone = VirGoTone.GRAY) })
            }
        }
        compose.onNodeWithText("Próximamente").assertIsDisplayed()
        compose.onNode(hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun emptyStateTitleIsHeadingAndActionWorks() {
        var acted = false
        compose.setContent {
            VirGoTheme {
                EmptyState(
                    title = "Aún no hay salones",
                    text = "Vuelve pronto",
                    iconVector = Icons.Outlined.Person,
                    action = { VirGoButton(onClick = { acted = true }, text = "Actualizar") },
                )
            }
        }
        compose.onNodeWithText("Aún no hay salones").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("Actualizar").performClick()
        compose.runOnIdle { assertTrue(acted) }
    }
}
