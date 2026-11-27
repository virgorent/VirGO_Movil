package mx.virgo.app.ui.studio

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import mx.virgo.app.ui.assistant.AssistantActions
import mx.virgo.app.ui.assistant.AssistantContent
import mx.virgo.app.ui.assistant.AssistantProblem
import mx.virgo.app.ui.assistant.AssistantUiState
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.ChatMessage
import mx.virgo.core.domain.DecorIdea
import mx.virgo.core.domain.StaffRole
import mx.virgo.core.testing.sampleEvent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class AiScreensTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(content: @androidx.compose.runtime.Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
                VirGoTheme { content() }
            }
        }
    }

    private val ready = StudioUiState(loading = false, event = sampleEvent(), guestsInput = "200", venueCapacity = 180)

    @Test
    fun tablesShowEachLayoutAndWarnWhenTheyDoNotFit() {
        show { StudioContent(ready, StudioActions()) }
        compose.onNodeWithText("Pista al centro", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("17 mesas de 12 personas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("No caben en esta distribución", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun staffShowsRulesAndAdjusts() {
        var change: Pair<StaffRole, Int>? = null
        val state =
            ready.copy(
                tab = StudioTab.STAFF,
                staff =
                    mapOf(
                        StaffRole.WAITERS to 9,
                        StaffRole.SECURITY to 2,
                        StaffRole.CLEANING to 3,
                    ),
            )
        show { StudioContent(state, StudioActions(onStaff = { role, delta -> change = role to delta })) }
        compose.onNodeWithText("1 por cada 30 invitados").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Volver a la sugerencia").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Más Meseros").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(StaffRole.WAITERS to 1, change) }
    }

    @Test
    fun ideasOverTheDecorationBudgetAreFlagged() {
        val ideas = listOf(DecorIdea("Arco floral", null, BigDecimal("20000")))
        show {
            StudioContent(
                ready.copy(tab = StudioTab.IDEAS, ideas = ideas, ideasStatus = IdeasStatus.READY),
                StudioActions(),
            )
        }
        compose
            .onNodeWithText(
                "Por encima del presupuesto de decoración",
                substring = true,
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun ideasWithoutEndpointSaySoon() {
        show {
            StudioContent(
                ready.copy(tab = StudioTab.IDEAS, ideasStatus = IdeasStatus.UNAVAILABLE),
                StudioActions(),
            )
        }
        compose.onNodeWithText("Las ideas con IA llegarán pronto.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun assistantSuggestionSendsAndUncertainRepliesAreMarked() {
        var sent: String? = null
        show { AssistantContent(AssistantUiState(), AssistantActions(onSend = { sent = it })) }
        compose.onNodeWithText("¿Cuántos meseros necesito?").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("¿Cuántos meseros necesito?", sent) }
    }

    @Test
    fun assistantShowsUncertaintyAndMissingEndpoint() {
        val state =
            AssistantUiState(
                messages =
                    listOf(
                        ChatMessage("¿Fechas?", true),
                        ChatMessage("Depende del salón.", false, uncertain = true),
                    ),
                problem = AssistantProblem.UNAVAILABLE,
            )
        show { AssistantContent(state, AssistantActions()) }
        compose.onNodeWithContentDescription("No estoy seguro", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("El asistente todavía no está conectado.").performScrollTo().assertIsDisplayed()
    }
}
