package mx.virgo.app.ui.bookings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingStatus

/**
 * «Mis reservaciones» del cliente: lista real y comprobante de cada una (009 RF-08/RF-09).
 * [reviewSlot] dibuja «Calificar el salón» (`019`) bajo el comprobante; vacío en pruebas sin Hilt.
 */
@Composable
fun ClientBookings(
    reviewSlot: @Composable (Booking) -> Unit = {},
    viewModel: BookingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (state.receipt != null) BackHandler(onBack = viewModel::closeReceipt)
    BookingsContent(
        state,
        onOpen = viewModel::open,
        onRetry = viewModel::refresh,
        onRetryReceipt = viewModel::open,
        folioActions = FolioSearchActions(onChange = viewModel::onFolioChange, onSearch = viewModel::searchFolio),
        reviewSlot = reviewSlot,
    )
}

@Composable
internal fun BookingsContent(
    state: BookingsUiState,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit,
    onRetryReceipt: (String) -> Unit = onOpen,
    folioActions: FolioSearchActions = FolioSearchActions(),
    reviewSlot: @Composable (Booking) -> Unit = {},
) {
    VirGoPageBody {
        val receipt = state.receipt
        if (receipt == null && !(state.loading && state.bookings.isEmpty())) {
            FolioSearch(state, folioActions)
        }
        when {
            receipt != null -> ReceiptFromList(receipt, onRetryReceipt, reviewSlot)
            state.loading && state.bookings.isEmpty() ->
                LoadingState(
                    message = stringResource(R.string.bookings_loading),
                )
            state.failed ->
                ErrorBanner(
                    message = stringResource(R.string.bookings_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = onRetry,
                )
            state.bookings.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.bookings_empty),
                    text = stringResource(R.string.bookings_empty_hint),
                    iconVector = Icons.Outlined.EventAvailable,
                )
            else -> BookingList(state.bookings, onOpen)
        }
    }
}

@Composable
private fun ReceiptFromList(
    receipt: ReceiptState,
    onRetry: (String) -> Unit,
    reviewSlot: @Composable (Booking) -> Unit,
) {
    val booking = receipt.booking
    when {
        booking != null -> {
            BookingReceiptBody(booking, onMyBookings = null, onCatalog = null)
            ChangesNotice()
            val venueId = booking.venueId
            if (venueId != null && booking.status != BookingStatus.CANCELLED) reviewSlot(booking)
        }
        receipt.failed ->
            ErrorBanner(
                message = stringResource(R.string.bookings_receipt_error),
                actionLabel = stringResource(R.string.booking_retry),
                onAction = { onRetry(receipt.bookingId) },
            )
        else -> LoadingState()
    }
}

@Composable
private fun BookingList(
    bookings: List<Booking>,
    onOpen: (String) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            bookings.forEachIndexed { index, booking ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = booking.venueName,
                    subtitle =
                        stringResource(
                            R.string.bookings_item,
                            booking.folio,
                            booking.date?.let(::formatDate).orEmpty(),
                        ),
                    icon = Icons.Outlined.EventAvailable,
                    tone = VirGoTone.VIOLET,
                    standalone = false,
                    onClick = { onOpen(booking.id) },
                    trailing = { BookingStatusBadge(booking.status) },
                )
            }
        }
    }
}
