package mx.virgo.app.ui.ownercalendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import mx.virgo.app.ui.ownerhome.OwnerHomeActions
import mx.virgo.app.ui.ownerhome.OwnerHomeContent
import mx.virgo.app.ui.ownerhome.OwnerHomeUiState
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.OwnerDashboard
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueDay
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.testing.sampleBooking
import mx.virgo.core.testing.sampleVenueDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class OwnerScreensTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.parse("2027-05-10")
    private val venue = OwnerVenue(sampleBooking().venueId!!, sampleVenueDraft(), VenueStatus.PUBLISHED)

    private fun show(content: @androidx.compose.runtime.Composable () -> Unit) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
                VirGoTheme { content() }
            }
        }
    }

    @Test
    fun homeShowsNumbersAndNextBookings() {
        var calendar = false
        val dashboard = OwnerDashboard(1, 2, 3, 1, listOf(sampleBooking()))
        show {
            OwnerHomeContent(
                OwnerHomeUiState(loading = false, dashboard = dashboard),
                "Mariana Salones",
                OwnerHomeActions(onCalendar = { calendar = true }),
                onRetry = {},
            )
        }
        compose.onNodeWithText("Hola, Mariana").assertIsDisplayed()
        compose.onNodeWithContentDescription("1 de 2 salones publicados").assertIsDisplayed()
        compose.onNodeWithContentDescription("3 solicitudes por responder").assertIsDisplayed()
        compose.onNodeWithText("Calendario").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(calendar) }
    }

    @Test
    fun homeWithoutVenuesOffersRegistering() {
        show {
            OwnerHomeContent(
                OwnerHomeUiState(loading = false, dashboard = OwnerDashboard(0, 0, 0, 0, emptyList())),
                "Mariana",
                OwnerHomeActions(),
                onRetry = {},
            )
        }
        compose.onNodeWithText("Aún no tienes salones").assertIsDisplayed()
    }

    @Test
    fun calendarDaysDescribeTheirStatusAndOverlapIsShown() {
        val booking = sampleBooking()
        val state =
            OwnerCalendarUiState(
                loading = false,
                venues = listOf(venue),
                venueId = venue.id,
                month = YearMonth.of(2027, 5),
                bookings = listOf(booking, booking.copy(id = "otra", folio = "VG-2027-0002")),
                venueDays = listOf(VenueDay(LocalDate.parse("2027-05-25"), blocked = true)),
                selected = LocalDate.parse("2027-05-15"),
            )
        show { OwnerCalendarContent(state, OwnerCalendarActions(), today) }
        compose.onNodeWithContentDescription("15 de mayo, Reservado").assertIsDisplayed()
        compose.onNodeWithContentDescription("25 de mayo, Bloqueado").assertIsDisplayed()
        // Hoy es 10 de mayo: un día anterior sin reserva ya no se ofrece como libre.
        compose.onNodeWithContentDescription("5 de mayo, Ya pasó").assertIsDisplayed()
        compose.onNodeWithContentDescription("20 de mayo, Libre").assertIsDisplayed()
        compose.onNodeWithText("Hay más de una reservación este día").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freeFutureDayOffersBlockingAndTheFormSends() {
        var opened = false
        var sent = false
        val base =
            OwnerCalendarUiState(
                loading = false,
                venues = listOf(venue),
                venueId = venue.id,
                month = YearMonth.of(2027, 5),
                selected = LocalDate.parse("2027-05-20"),
            )
        show {
            OwnerCalendarContent(
                base.copy(blockForm = BlockForm(reason = mx.virgo.core.domain.BlockReason.MAINTENANCE)),
                OwnerCalendarActions(onOpenBlock = { opened = true }, onConfirmBlock = { sent = true }),
                today,
            )
        }
        compose.onNodeWithText("Mantenimiento").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Bloquear").performScrollTo().performClick()
        compose.runOnIdle {
            assertTrue(sent)
            assertEquals(false, opened)
        }
    }

    @Test
    fun calendarWithoutPublishedVenuesExplainsWhy() {
        show { OwnerCalendarContent(OwnerCalendarUiState(loading = false), OwnerCalendarActions(), today) }
        compose.onNodeWithText("Sin salones publicados").assertIsDisplayed()
    }
}
