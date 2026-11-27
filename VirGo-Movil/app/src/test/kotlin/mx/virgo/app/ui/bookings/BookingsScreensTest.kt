package mx.virgo.app.ui.bookings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.BookingField
import mx.virgo.core.domain.BookingProblem
import mx.virgo.core.testing.sampleBooking
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h2000dp")
class BookingsScreensTest {
    @get:Rule
    val compose = createComposeRule()

    private val ready =
        BookingConfirmUiState(
            draft = BookingDraft(venueId = "v1", date = "2027-05-15", hours = "5"),
            venue = sampleVenueDetail(),
            loadingVenue = false,
        )

    @Test
    fun confirmShowsTheNoChargeNoticeAndSends() {
        var confirmed = 0
        compose.setContent {
            VirGoTheme { BookingConfirmContent(ready, BookingConfirmActions(onConfirm = { confirmed++ })) }
        }
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
        compose.onNodeWithText("No se cobra nada en la app", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("El salón pide al menos 4 horas.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Disponible", substring = true).assertDoesNotExist()
        compose.onNode(hasText("Confirmar reservación") and hasClickAction()).performScrollTo().performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun withAQuoteShowsTheQuotedTotalAndDateWithoutPicker() {
        compose.setContent {
            VirGoTheme {
                BookingConfirmContent(
                    ready.copy(draft = ready.draft.copy(quoteId = "q-9", quotedTotal = "48000")),
                    BookingConfirmActions(),
                )
            }
        }
        compose.onNodeWithText("Total cotizado por el salón", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Fecha cotizada", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Precio de referencia", substring = true).assertDoesNotExist()
    }

    @Test
    fun fieldErrorsAndServerRejectionAreShownNextToTheirField() {
        compose.setContent {
            VirGoTheme {
                BookingConfirmContent(
                    ready.copy(fieldErrors = mapOf(BookingField.DATE to BookingProblem.PAST_DATE), rejected = true),
                    BookingConfirmActions(),
                )
            }
        }
        compose.onNodeWithText("La fecha no puede estar en el pasado.").performScrollTo().assertIsDisplayed()
        compose
            .onNodeWithText(
                "VirGo no aceptó la fecha o las horas",
                substring = true,
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun receiptIsHonestAboutPaymentAndDate() {
        val actions = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                BookingConfirmContent(
                    ready.copy(booking = sampleBooking()),
                    BookingConfirmActions(onMyBookings = { actions += "mine" }, onCatalog = { actions += "catalog" }),
                )
            }
        }
        compose.onNodeWithText("Reservación registrada").assertIsDisplayed()
        compose.onNodeWithText("Folio VG-2027-0001").assertIsDisplayed()
        compose.onNodeWithText("Pendiente de pago").assertIsDisplayed()
        compose.onNodeWithText("No se ha cobrado nada", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver mis reservaciones").performScrollTo().performClick()
        compose.onNodeWithText("Volver al catálogo").performScrollTo().performClick()
        assertEquals(listOf("mine", "catalog"), actions)
    }

    @Test
    fun listOpensAReceiptAndEmptyStateIsClear() {
        var opened: String? = null
        compose.setContent {
            VirGoTheme {
                BookingsContent(
                    BookingsUiState(loading = false, bookings = listOf(sampleBooking())),
                    onOpen = { opened = it },
                    onRetry = {},
                )
            }
        }
        compose.onNodeWithText("Salón Las Palmas").performClick()
        assertEquals(sampleBooking().id, opened)
    }

    @Test
    fun emptyListSaysSo() {
        compose.setContent { VirGoTheme { BookingsContent(BookingsUiState(loading = false), {}, {}) } }
        compose.onNodeWithText("Aún no tienes reservaciones").assertIsDisplayed()
    }

    @Test
    fun receiptFromTheListComesFromTheServer() {
        compose.setContent {
            VirGoTheme {
                BookingsContent(
                    BookingsUiState(
                        loading = false,
                        receipt = ReceiptState(sampleBooking().id, loading = false, booking = sampleBooking()),
                    ),
                    {},
                    {},
                )
            }
        }
        compose.onNodeWithText("Folio VG-2027-0001").assertIsDisplayed()
        compose.onNodeWithText("Ver mis reservaciones").assertDoesNotExist()
    }

    @Test
    fun folioSearchShowsFormatErrorAndSearches() {
        var searched = false
        compose.setContent {
            VirGoTheme {
                BookingsContent(
                    BookingsUiState(loading = false, folioQuery = "abc", folioError = FolioError.FORMAT),
                    onOpen = {},
                    onRetry = {},
                    folioActions = FolioSearchActions(onSearch = { searched = true }),
                )
            }
        }
        compose.onNodeWithText("por ejemplo VG-2026-0001", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Buscar").performClick()
        assertTrue(searched)
    }

    @Test
    fun unknownFolioMessageDoesNotHideTheList() {
        compose.setContent {
            VirGoTheme {
                BookingsContent(
                    BookingsUiState(
                        loading = false,
                        bookings = listOf(sampleBooking()),
                        folioError = FolioError.NOT_FOUND,
                    ),
                    {},
                    {},
                )
            }
        }
        compose.onNodeWithText("No encontramos ese folio en tus reservaciones").assertIsDisplayed()
        compose.onNodeWithText("Salón Las Palmas").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun receiptExplainsChangesWithoutCancelButtons() {
        compose.setContent {
            VirGoTheme {
                BookingsContent(
                    BookingsUiState(
                        loading = false,
                        receipt = ReceiptState(sampleBooking().id, loading = false, booking = sampleBooking()),
                    ),
                    {},
                    {},
                )
            }
        }
        compose.onNodeWithText("¿Necesitas cancelar o cambiar la fecha?").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Buscar por folio").assertDoesNotExist()
        compose.onAllNodes(hasClickAction() and hasText("Cancelar", substring = true)).assertCountEquals(0)
        compose.onAllNodes(hasClickAction() and hasText("Reprogramar", substring = true)).assertCountEquals(0)
        compose.onAllNodes(hasText("reembolso", substring = true, ignoreCase = true)).assertCountEquals(0)
    }
}
