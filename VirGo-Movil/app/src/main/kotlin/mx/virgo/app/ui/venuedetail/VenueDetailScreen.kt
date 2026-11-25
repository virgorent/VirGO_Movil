package mx.virgo.app.ui.venuedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.theme.pageBackground

@Composable
fun VenueDetailScreen(
    venueId: String,
    actions: VenueDetailActions,
    viewModel: VenueDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(venueId) { viewModel.load(venueId) }
    val state by viewModel.state.collectAsState()
    VenueDetailContent(state, actions.copy(onRetry = viewModel::retry))
}

@Composable
internal fun VenueDetailContent(
    state: VenueDetailUiState,
    actions: VenueDetailActions,
) {
    Column(Modifier.fillMaxSize().background(pageBackground())) {
        VirGoPageHeader(
            title = state.venue?.name ?: stringResource(R.string.venue_detail_title),
            kicker = stringResource(R.string.venue_detail_kicker),
            onBack = actions.onBack,
            backContentDescription = stringResource(R.string.venue_detail_back),
        )
        val venue = state.venue
        when {
            venue != null -> VirGoPageBody { VenueDetailBody(venue, state.viewer, actions) }
            state.loading -> LoadingState(message = stringResource(R.string.venue_detail_loading))
            else -> DetailProblem(state.error, actions)
        }
    }
}

@Composable
private fun DetailProblem(
    error: AppError?,
    actions: VenueDetailActions,
) {
    val missing = error == AppError.NotFound
    VirGoPageBody {
        VirGoCard {
            EmptyState(
                title = stringResource(if (missing) R.string.venue_detail_not_found else R.string.venue_detail_error),
                text =
                    stringResource(
                        if (missing) R.string.venue_detail_not_found_hint else R.string.venue_detail_error_hint,
                    ),
                iconVector = if (missing) Icons.Outlined.SearchOff else Icons.Outlined.CloudOff,
                action = {
                    if (missing) {
                        VirGoButton(onClick = actions.onBack, text = stringResource(R.string.venue_detail_back))
                    } else {
                        VirGoButton(onClick = actions.onRetry, text = stringResource(R.string.venue_detail_retry))
                    }
                },
            )
        }
    }
}

/** Acciones según quién mira (006 RF-07–RF-09). */
@Composable
internal fun DetailActions(
    venueId: String,
    viewer: DetailViewer,
    actions: VenueDetailActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (viewer) {
            DetailViewer.GUEST -> {
                Hint(R.string.venue_detail_guest_hint)
                VirGoButton(onClick = actions.onSignIn, text = stringResource(R.string.venue_detail_quote))
                VirGoOutlinedButton(onClick = actions.onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.venue_detail_book))
                }
            }
            DetailViewer.CLIENT -> {
                val quote = actions.onQuote
                val book = actions.onBook
                if (quote == null || book == null) Hint(R.string.venue_detail_actions_soon)
                VirGoButton(
                    onClick = { quote?.invoke(venueId) },
                    enabled = quote != null,
                    text = stringResource(R.string.venue_detail_quote),
                )
                VirGoOutlinedButton(
                    onClick = { book?.invoke(venueId) },
                    enabled = book != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.venue_detail_book)) }
            }
            DetailViewer.OTHER_ROLE -> Hint(R.string.venue_detail_client_only)
        }
    }
}

@Composable
private fun Hint(text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun Modifier.fillMaxWidth(): Modifier = this.then(Modifier.fillMaxWidth())
