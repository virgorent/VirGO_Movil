package mx.virgo.app.ui.quotes

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GetQuoteUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.QuoteField
import mx.virgo.core.domain.QuoteProblem
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.testing.FakeQuotesRepository
import mx.virgo.core.testing.sampleAnsweredQuote
import mx.virgo.core.testing.sampleQuote
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h2000dp")
class QuotesScreensTest {
    @get:Rule
    val compose = createComposeRule()

    private val ready =
        QuoteRequestUiState(
            draft = QuoteDraft(venueId = "v1", date = "2027-05-15", guests = "120"),
            venue = sampleVenueDetail(),
            loadingVenue = false,
        )

    @Test
    fun requestShowsTheVenueFieldsAndSends() {
        val events = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                QuoteRequestContent(
                    ready,
                    QuoteRequestActions(onEventType = { events += it.wire }, onSend = { events += "send" }),
                )
            }
        }
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
        compose.onNodeWithText("XV años").performClick()
        compose.onNodeWithText("0 de 500 caracteres").performScrollTo().assertIsDisplayed()
        compose.onNode(hasText("Enviar solicitud") and hasClickAction()).performScrollTo().performClick()
        assertEquals(listOf("XV años", "send"), events)
    }

    @Test
    fun errorsAndCapacityWarningAreShownNextToTheirField() {
        compose.setContent {
            VirGoTheme {
                QuoteRequestContent(
                    ready.copy(
                        draft = ready.draft.copy(guests = "900"),
                        fieldErrors = mapOf(QuoteField.EVENT_TYPE to QuoteProblem.REQUIRED),
                        serverErrors = mapOf(QuoteField.DATE to "La fecha del evento no puede estar en el pasado."),
                        error = QuoteError.NETWORK,
                    ),
                    QuoteRequestActions(),
                )
            }
        }
        compose.onNodeWithText("Elige el tipo de evento.").assertIsDisplayed()
        compose.onNodeWithText("La fecha del evento no puede estar en el pasado.").assertIsDisplayed()
        compose.onNodeWithText("recibe hasta", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("lo que escribiste se conserva", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sendingDisablesTheButton() {
        compose.setContent {
            VirGoTheme { QuoteRequestContent(ready.copy(sending = true), QuoteRequestActions()) }
        }
        compose.onNodeWithText("Enviando…").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun sentOffersMyQuotes() {
        var mine = 0
        compose.setContent {
            VirGoTheme {
                QuoteRequestContent(ready.copy(sent = sampleQuote()), QuoteRequestActions(onMyQuotes = { mine++ }))
            }
        }
        compose.onNodeWithText("Ver mis cotizaciones").performClick()
        assertEquals(1, mine)
    }

    @Test
    fun listShowsRealStatusesAndOpensADetail() {
        var opened: String? = null
        compose.setContent {
            VirGoTheme {
                QuotesContent(
                    QuotesUiState(
                        loading = false,
                        quotes =
                            listOf(
                                sampleQuote(),
                                sampleAnsweredQuote().copy(id = "a"),
                                sampleQuote().copy(id = "x", status = QuoteStatus.UNKNOWN),
                            ),
                    ),
                    QuotesActions(onOpen = { opened = it }),
                )
            }
        }
        compose.onNodeWithText("Enviada").assertIsDisplayed()
        compose.onNodeWithText("Respondida").assertIsDisplayed()
        compose.onNodeWithText("En revisión").assertIsDisplayed()
        compose.onNodeWithText("Respondida").performClick()
        assertEquals("a", opened)
    }

    @Test
    fun emptyListLeadsToTheCatalog() {
        var catalog = 0
        compose.setContent {
            VirGoTheme { QuotesContent(QuotesUiState(loading = false), QuotesActions(onCatalog = { catalog++ })) }
        }
        compose.onNodeWithText("Aún no has pedido cotizaciones").assertIsDisplayed()
        compose.onNodeWithText("Ir al catálogo").performClick()
        assertEquals(1, catalog)
    }

    @Test
    fun sentQuoteWaitsWithoutBooking() {
        compose.setContent {
            VirGoTheme {
                QuotesContent(
                    QuotesUiState(detail = QuoteDetailState(sampleQuote().id, false, sampleQuote())),
                    QuotesActions(),
                )
            }
        }
        compose.onNodeWithText("El salón aún no responde.").assertIsDisplayed()
        compose.onNodeWithText("Reservar").assertDoesNotExist()
    }

    @Test
    fun answeredQuoteShowsServerAmountsAndBooks() {
        var booked: String? = null
        compose.setContent {
            VirGoTheme {
                QuotesContent(
                    QuotesUiState(detail = QuoteDetailState(sampleAnsweredQuote().id, false, sampleAnsweredQuote())),
                    QuotesActions(onBook = { booked = it.id }),
                )
            }
        }
        compose.onNodeWithText("48,000", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Incluye mobiliario y limpieza.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Reservar").performScrollTo().performClick()
        assertEquals(sampleAnsweredQuote().id, booked)
    }

    @Test
    fun clientQuotesOpensBookingWithTheQuote() {
        val repository = FakeQuotesRepository().apply { quotes += sampleAnsweredQuote() }
        val vm =
            QuotesViewModel(
                GetQuotesUseCase(repository),
                GetQuoteUseCase(repository),
                SessionRepository(FakeSessionTokenStore(), Clock.systemUTC()),
            )
        compose.setContent {
            VirGoTheme {
                ClientQuotes(
                    onCatalog = {},
                    onMyBookings = {},
                    viewModel = vm,
                    bookingStep = { quote, back ->
                        TextButton(onClick = back) { Text("Reservar ${quote.id} ${quote.eventDate}") }
                    },
                )
            }
        }
        compose.onNodeWithText("Mis cotizaciones").assertIsDisplayed()
        compose.onNodeWithText("Salón Las Palmas").performClick()
        compose.onNodeWithText("Reservar").performScrollTo().performClick()
        // La confirmación trae su propio encabezado: el de la lista no queda encima.
        compose.onNodeWithText("Mis cotizaciones").assertDoesNotExist()
        compose.onNodeWithText("Reservar ${sampleAnsweredQuote().id} 2027-05-15").assertIsDisplayed().performClick()
        compose.onNodeWithText("Incluye mobiliario y limpieza.").performScrollTo().assertIsDisplayed()
        // La flecha del detalle vuelve a la lista, no sale de «Mis cotizaciones».
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Incluye mobiliario y limpieza.").assertDoesNotExist()
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
    }
}
