package mx.virgo.app.ui.ownerrequests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.AnswerField
import mx.virgo.core.domain.AnswerProblem
import mx.virgo.core.testing.sampleAnsweredQuote
import mx.virgo.core.testing.sampleQuote
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h2000dp")
class OwnerRequestsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun listShowsClientAndTabsSwitch() {
        val events = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                OwnerRequestsContent(
                    OwnerRequestsUiState(loading = false, quotes = listOf(sampleQuote())),
                    OwnerRequestsActions(onTab = { events += it.name }, onOpen = { events += it }),
                )
            }
        }
        compose.onNodeWithText("Ana López", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Enviada").assertIsDisplayed()
        compose.onNodeWithText("Salón Las Palmas").performClick()
        compose.onNodeWithText("Respondidas").performClick()
        assertEquals(listOf(sampleQuote().id, "ANSWERED"), events)
    }

    @Test
    fun emptyTabSaysSo() {
        compose.setContent {
            VirGoTheme {
                OwnerRequestsContent(
                    OwnerRequestsUiState(loading = false, tab = RequestsTab.ANSWERED),
                    OwnerRequestsActions(),
                )
            }
        }
        compose.onNodeWithText("Aún no has respondido solicitudes").assertIsDisplayed()
    }

    @Test
    fun sentRequestShowsTheFormAndSends() {
        val events = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                OwnerRequestsContent(
                    OwnerRequestsUiState(
                        detail = RequestDetailState(sampleQuote().id, loading = false, quote = sampleQuote()),
                    ),
                    OwnerRequestsActions(onForm = { events += it.total }, onSend = { events += "send" }),
                )
            }
        }
        compose.onNodeWithText("Ana López").assertIsDisplayed()
        compose.onNodeWithText("El salón aún no responde.").assertDoesNotExist()
        compose.onNodeWithContentDescription("Total en pesos").performScrollTo().performTextInput("48000")
        compose.onNode(hasText("Enviar al cliente") and hasClickAction()).performScrollTo().performClick()
        assertEquals(listOf("48000", "send"), events)
    }

    @Test
    fun fieldErrorsAndConflictAreShown() {
        compose.setContent {
            VirGoTheme {
                OwnerRequestsContent(
                    OwnerRequestsUiState(
                        detail =
                            RequestDetailState(
                                sampleQuote().id,
                                loading = false,
                                quote = sampleQuote(),
                                fieldErrors = mapOf(AnswerField.DEPOSIT to AnswerProblem.ABOVE_TOTAL),
                                error = AnswerError.ALREADY_ANSWERED,
                            ),
                    ),
                    OwnerRequestsActions(),
                )
            }
        }
        compose.onNodeWithText("Esta solicitud ya fue respondida.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("El anticipo no puede ser mayor que el total.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun answeredRequestIsReadOnly() {
        compose.setContent {
            VirGoTheme {
                OwnerRequestsContent(
                    OwnerRequestsUiState(
                        detail =
                            RequestDetailState(
                                sampleAnsweredQuote().id,
                                loading = false,
                                quote = sampleAnsweredQuote(),
                                justAnswered = true,
                            ),
                    ),
                    OwnerRequestsActions(),
                )
            }
        }
        compose.onNodeWithText("48,000", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Respuesta enviada", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Enviar al cliente").assertDoesNotExist()
        compose.onNodeWithText("Reservar").assertDoesNotExist()
    }
}
