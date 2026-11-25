package mx.virgo.app.ui.venuedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.ui.beta.BetaCatalogContent
import mx.virgo.app.ui.beta.BetaCatalogState
import mx.virgo.core.common.AppError
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Venue
import mx.virgo.core.testing.sampleVenueDetail
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
class VenueDetailScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val venue = sampleVenueDetail()

    private fun show(
        state: VenueDetailUiState,
        actions: VenueDetailActions = VenueDetailActions(),
    ) {
        compose.setContent { VirGoTheme { VenueDetailContent(state, actions) } }
    }

    private fun loaded(viewer: DetailViewer) =
        VenueDetailUiState(venueId = venue.id, loading = false, venue = venue, viewer = viewer)

    @Test
    fun catalogOpensTheDetailWithTheVenueId() {
        var opened: String? = null
        val listed = Venue("real-id", "Salón Real", null, "Centro", 100, 0.0, 0, null, null)
        compose.setContent {
            VirGoTheme {
                BetaCatalogContent(BetaCatalogState(loading = false, venues = listOf(listed)), {}, onOpenVenue = {
                    opened = it
                })
            }
        }
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        assertEquals("real-id", opened)
    }

    @Test
    fun guestSeesNoPriceAndActionsLeadToSignIn() {
        var signIn = 0
        show(loaded(DetailViewer.GUEST), VenueDetailActions(onSignIn = { signIn++ }))
        compose.onNodeWithText("Inicia sesión para ver precios").assertIsDisplayed()
        compose.onNodeWithText("Solicitar cotización").performScrollTo().performClick()
        compose.onNodeWithText("Reservar").performScrollTo().performClick()
        assertEquals(2, signIn)
    }

    @Test
    fun clientSeesPriceAndActionsWithTheVenueId() {
        val opened = mutableListOf<String>()
        show(
            loaded(DetailViewer.CLIENT),
            VenueDetailActions(onQuote = { opened += "quote:$it" }, onBook = { opened += "book:$it" }),
        )
        compose.onNodeWithText("por día", substring = true).assertIsDisplayed()
        compose
            .onNodeWithText("Solicitar cotización")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        compose.onNodeWithText("Reservar").performScrollTo().performClick()
        assertEquals(listOf("quote:${venue.id}", "book:${venue.id}"), opened)
    }

    @Test
    fun clientActionsWaitForQuotesAndBookingsModules() {
        show(loaded(DetailViewer.CLIENT))
        compose.onNodeWithText("Solicitar cotización").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("siguiente actualización", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun ownerOrProviderDoNotSeeClientActions() {
        show(loaded(DetailViewer.OTHER_ROLE))
        compose.onNodeWithText("Disponible para cuentas de cliente.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Solicitar cotización").assertDoesNotExist()
    }

    @Test
    fun zeroCapacityAndNoReviewsAreHonest() {
        show(
            VenueDetailUiState(
                venueId = venue.id,
                loading = false,
                venue = venue.copy(capacity = 0, priceBase = null),
                viewer = DetailViewer.CLIENT,
            ),
        )
        compose.onNodeWithText("Sin reseñas todavía").assertIsDisplayed()
        compose.onNodeWithText("Precio a cotizar").assertIsDisplayed()
        compose.onNodeWithText("personas", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Verificado").assertDoesNotExist()
    }

    @Test
    fun notFoundOffersToGoBack() {
        var back = false
        show(
            VenueDetailUiState(venueId = "x", loading = false, error = AppError.NotFound),
            VenueDetailActions(onBack = { back = true }),
        )
        compose.onNodeWithText("Este salón ya no está disponible").assertIsDisplayed()
        compose.onNodeWithText("Volver al catálogo").performClick()
        assertEquals(true, back)
    }

    @Test
    fun networkErrorOffersRetryWithoutTechnicalText() {
        var retried = false
        show(
            VenueDetailUiState(venueId = "x", loading = false, error = AppError.Server("trace-123")),
            VenueDetailActions(onRetry = { retried = true }),
        )
        compose.onNodeWithText("No pudimos cargar el salón").assertIsDisplayed()
        compose.onNodeWithText("trace-123", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Reintentar").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun clientExploreGoesFromCatalogToDetailToQuoteOrBookingAndBack() {
        compose.setContent {
            VirGoTheme {
                ClientExplore(
                    catalog = { open -> TextButton(onClick = { open("v7") }) { Text("Catálogo") } },
                    detail = { id, back, step ->
                        Column {
                            TextButton(onClick = back) { Text("Detalle $id") }
                            TextButton(onClick = step.quote) { Text("Cotizar $id") }
                            TextButton(onClick = step.book) { Text("Reservar $id") }
                        }
                    },
                    quote = { id, back -> TextButton(onClick = back) { Text("Solicitud $id") } },
                    booking = { id, back, toCatalog ->
                        Column {
                            TextButton(onClick = back) { Text("Confirmar $id") }
                            TextButton(onClick = toCatalog) { Text("Al catálogo") }
                        }
                    },
                )
            }
        }
        compose.onNodeWithText("Catálogo").performClick()
        compose.onNodeWithText("Cotizar v7").performClick()
        compose.onNodeWithText("Solicitud v7").assertIsDisplayed().performClick()
        compose.onNodeWithText("Reservar v7").performClick()
        compose.onNodeWithText("Confirmar v7").assertIsDisplayed().performClick()
        compose.onNodeWithText("Detalle v7").assertIsDisplayed().performClick()
        compose.onNodeWithText("Catálogo").assertIsDisplayed().performClick()
        compose.onNodeWithText("Reservar v7").performClick()
        compose.onNodeWithText("Al catálogo").performClick()
        compose.onNodeWithText("Catálogo").assertIsDisplayed()
    }
}
