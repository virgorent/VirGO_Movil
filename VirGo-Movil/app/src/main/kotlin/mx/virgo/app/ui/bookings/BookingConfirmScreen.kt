package mx.virgo.app.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.venuedetail.DetailViewer
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.app.ui.venuedetail.priceText
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.designsystem.theme.pageBackground
import mx.virgo.core.domain.BookingField
import mx.virgo.core.domain.BookingProblem
import java.math.BigDecimal
import java.time.LocalDate

/** C8 → C23: confirma y, al registrarse, muestra el comprobante del servidor. */
@Composable
fun BookingConfirmScreen(
    venueId: String,
    actions: BookingConfirmActions,
    quoteId: String? = null,
    date: LocalDate? = null,
    quotedTotal: BigDecimal? = null,
    viewModel: BookingConfirmViewModel = hiltViewModel(),
) {
    LaunchedEffect(venueId, quoteId) { viewModel.start(venueId, quoteId, date, quotedTotal) }
    val state by viewModel.state.collectAsState()
    BookingConfirmContent(
        state,
        actions.copy(
            onDate = viewModel::setDate,
            onHours = viewModel::setHours,
            onConfirm = viewModel::confirm,
            onRetryVenue = viewModel::loadVenue,
        ),
    )
}

@Composable
internal fun BookingConfirmContent(
    state: BookingConfirmUiState,
    actions: BookingConfirmActions,
) {
    val booking = state.booking
    Column(Modifier.fillMaxSize().background(pageBackground())) {
        VirGoPageHeader(
            title =
                stringResource(
                    if (booking !=
                        null
                    ) {
                        R.string.booking_receipt_title
                    } else {
                        R.string.booking_confirm_title
                    },
                ),
            kicker = stringResource(R.string.booking_kicker),
            onBack = actions.onBack,
            backContentDescription = stringResource(R.string.booking_back),
        )
        VirGoPageBody {
            when {
                booking != null -> BookingReceiptBody(booking, actions.onMyBookings, actions.onCatalog)
                state.error == BookingError.NOT_FOUND && state.venue == null ->
                    EmptyState(title = stringResource(R.string.booking_error_not_found))
                state.loadingVenue -> LoadingState()
                state.venueFailed ->
                    ErrorBanner(
                        message = stringResource(R.string.booking_venue_error),
                        actionLabel = stringResource(R.string.booking_retry),
                        onAction = actions.onRetryVenue,
                    )
                else -> ConfirmForm(state, actions)
            }
        }
    }
}

@Composable
private fun ConfirmForm(
    state: BookingConfirmUiState,
    actions: BookingConfirmActions,
) {
    val venue = state.venue ?: return
    Text(venue.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    val quoted = state.draft.quotedTotal?.toBigDecimalOrNull()
    Text(
        if (quoted != null) {
            stringResource(R.string.booking_quoted_total, formatMxn(quoted.toDouble()))
        } else {
            stringResource(R.string.booking_price_reference, priceText(venue, DetailViewer.CLIENT))
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (state.draft.quoteId != null) {
        Text(
            stringResource(R.string.booking_quoted_date, state.date?.let(::formatDate).orEmpty()),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    } else {
        DateField(state.date, state.fieldErrors[BookingField.DATE], actions.onDate)
    }
    HoursField(state, actions.onHours)
    VirGoCard {
        VirGoRow(
            title = stringResource(R.string.booking_no_charge),
            icon = Icons.Outlined.Info,
            tone = VirGoTone.VIOLET,
            standalone = false,
        )
    }
    state.error?.let { ErrorBanner(message = stringResource(bookingErrorText(it))) }
    VirGoButton(
        onClick = actions.onConfirm,
        enabled = !state.sending,
        text = stringResource(if (state.sending) R.string.booking_sending else R.string.booking_confirm),
    )
}

@Composable
private fun DateField(
    date: LocalDate?,
    problem: BookingProblem?,
    onDate: (LocalDate) -> Unit,
) {
    val errorText =
        when (problem) {
            BookingProblem.PAST_DATE -> stringResource(R.string.booking_error_date_past)
            null -> null
            else -> stringResource(R.string.booking_error_date_required)
        }
    EventDateField(stringResource(R.string.booking_date), date, errorText, onDate)
}

@Composable
private fun HoursField(
    state: BookingConfirmUiState,
    onHours: (String) -> Unit,
) {
    val label = stringResource(R.string.booking_hours)
    val minimum = state.venue?.minimumHours?.takeIf { it > 1 }
    val errorText =
        when {
            state.rejected -> stringResource(R.string.booking_error_rejected)
            state.fieldErrors[BookingField.HOURS] == BookingProblem.BELOW_MINIMUM ->
                stringResource(R.string.booking_error_hours_minimum, minimum ?: 1)
            state.fieldErrors[BookingField.HOURS] != null -> stringResource(R.string.booking_error_hours_required)
            else -> null
        }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        VirGoTextField(
            value = state.draft.hours,
            onValueChange = onHours,
            isError = errorText != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        )
        when {
            errorText != null -> FieldError(errorText)
            minimum != null ->
                Text(
                    stringResource(R.string.booking_hours_hint, minimum),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

@Composable
private fun FieldError(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

private fun bookingErrorText(error: BookingError): Int =
    when (error) {
        BookingError.NETWORK -> R.string.booking_error_network
        BookingError.NOT_FOUND -> R.string.booking_error_not_found
        BookingError.FORBIDDEN -> R.string.booking_error_forbidden
        BookingError.SERVER -> R.string.booking_error_server
    }
