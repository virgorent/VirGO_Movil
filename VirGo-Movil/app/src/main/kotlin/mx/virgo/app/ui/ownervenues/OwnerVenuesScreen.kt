package mx.virgo.app.ui.ownervenues

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueStatus

/**
 * «Mis salones» del propietario (022 RF-01) y su asistente. El asistente abierto sobrevive a la
 * rotación: la clave del registro se guarda aquí y los datos en [VenueWizardViewModel].
 */
@Composable
fun OwnerVenuesRoute(
    listViewModel: OwnerVenuesViewModel = hiltViewModel(),
    wizardViewModel: VenueWizardViewModel = hiltViewModel(),
) {
    val listState by listViewModel.state.collectAsState()
    var openKey by rememberSaveable { mutableStateOf<String?>(null) }
    val key = openKey
    if (key == null) {
        OwnerVenuesContent(
            state = listState,
            onRetry = { listViewModel.refresh() },
            onRegister = { openKey = "new-${System.currentTimeMillis()}" },
            onOpen = { venue -> openKey = "edit-${venue.id}-${System.currentTimeMillis()}" },
        )
    } else {
        val editing = listState.venues.firstOrNull { key.startsWith("edit-${it.id}-") }
        VenueWizardRoute(
            key = key,
            venue = editing,
            viewModel = wizardViewModel,
            onClose = { outcome ->
                openKey = null
                listViewModel.refresh(outcome)
            },
        )
    }
}

@Composable
internal fun OwnerVenuesContent(
    state: OwnerVenuesUiState,
    onRetry: () -> Unit,
    onRegister: () -> Unit,
    onOpen: (OwnerVenue) -> Unit,
) {
    VirGoPageBody {
        state.outcome?.let { OutcomeNotice(it) }
        when {
            state.loading && state.venues.isEmpty() ->
                LoadingState(
                    message = stringResource(R.string.owner_venues_loading),
                )
            state.error != null ->
                ErrorBanner(
                    message = listErrorText(state.error),
                    actionLabel = stringResource(R.string.venue_error_retry),
                    onAction = onRetry,
                )
            state.venues.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.owner_venues_empty_title),
                    text = stringResource(R.string.owner_venues_empty_text),
                    iconVector = Icons.Outlined.Storefront,
                    action = {
                        VirGoButton(
                            onClick = onRegister,
                            text = stringResource(R.string.owner_venues_register),
                        )
                    },
                )
            else -> VenueList(state.venues, onRegister, onOpen)
        }
    }
}

@Composable
private fun VenueList(
    venues: List<OwnerVenue>,
    onRegister: () -> Unit,
    onOpen: (OwnerVenue) -> Unit,
) {
    VirGoButton(onClick = onRegister, text = stringResource(R.string.owner_venues_register))
    VirGoSection(
        title = stringResource(R.string.owner_venues_section),
        kicker = stringResource(R.string.owner_venues_kicker),
    ) {
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                venues.forEachIndexed { index, venue ->
                    if (index > 0) VirGoRowDivider()
                    VirGoRow(
                        title = venue.draft.name,
                        subtitle = venueSubtitle(venue),
                        icon = Icons.Outlined.Storefront,
                        tone = if (venue.status == VenueStatus.PUBLISHED) VirGoTone.GREEN else VirGoTone.AMBER,
                        standalone = false,
                        onClick = { onOpen(venue) },
                        trailing = { StatusBadge(venue.status) },
                    )
                }
            }
        }
    }
}

@Composable
private fun venueSubtitle(venue: OwnerVenue): String {
    val zone = venue.draft.zone.orEmpty()
    val capacity = venue.draft.capacity ?: return zone
    return stringResource(R.string.owner_venues_capacity_summary, zone, capacity)
}

@Composable
internal fun StatusBadge(status: VenueStatus) {
    val (text, tone) =
        when (status) {
            VenueStatus.DRAFT -> R.string.owner_venues_status_draft to VirGoTone.GRAY
            VenueStatus.REVIEW -> R.string.owner_venues_status_review to VirGoTone.AMBER
            VenueStatus.PUBLISHED -> R.string.owner_venues_status_published to VirGoTone.GREEN
        }
    VirGoBadge(text = stringResource(text), tone = tone)
}

@Composable
private fun OutcomeNotice(outcome: VenueWizardOutcome) {
    VirGoCard(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        VirGoRow(
            title =
                stringResource(
                    if (outcome == VenueWizardOutcome.PUBLISHED) {
                        R.string.owner_venues_published_message
                    } else {
                        R.string.owner_venues_saved_message
                    },
                ),
            icon = Icons.Outlined.CheckCircle,
            tone = VirGoTone.GREEN,
            standalone = false,
        )
    }
}

@Composable
private fun listErrorText(error: AppError): String =
    stringResource(
        when (error) {
            AppError.Network, AppError.Timeout -> R.string.venue_error_network
            AppError.Forbidden -> R.string.venue_error_forbidden
            else -> R.string.venue_error_server
        },
    )
