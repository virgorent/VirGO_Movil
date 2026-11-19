package mx.virgo.app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.GuestMap
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900

private val CardHPadding = 16.dp
private val CardVPadding = 12.dp
private val CardPadding = 16.dp
private val ContentGap = 12.dp
private val MetaGap = 8.dp
private val SummaryPhotoSize = 96.dp

/** B8: mapa con los salones cercanos (RF-01) y la tarjeta inferior del salÃ³n elegido (RF-02). */
@Composable
internal fun ExploreMapContent(
    state: ExploreUiState,
    onVenueClick: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onFavoriteToggle: (VenueSample) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GuestMap(
            nearby = state.results,
            radiusKm = state.filters.radiusKm,
            selectedId = state.selectedVenueId,
            onVenueClick = onVenueClick,
            onClearSelection = onClearSelection,
        )
        val selected = state.selectedVenue
        val bottomModifier = Modifier.align(Alignment.BottomCenter)
        when {
            state.hasError ->
                ExploreErrorState(
                    onRetry = onRetry,
                    modifier = bottomModifier.fillMaxWidth(),
                )
            selected != null ->
                ExploreVenueSummaryCard(
                    venue = selected,
                    isFavorite = selected.venueId in state.favoriteIds,
                    onFavoriteToggle = { onFavoriteToggle(selected) },
                    onClose = onClearSelection,
                    modifier = bottomModifier,
                )
            state.results.isEmpty() && !state.isLoading ->
                ExploreEmptyState(modifier = bottomModifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ExploreVenueSummaryCard(
    venue: VenueSample,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VirGoCard(
        containerColor = Color.White,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CardHPadding,
                    vertical = CardVPadding,
                ),
    ) {
        Row(
            modifier = Modifier.padding(CardPadding),
            horizontalArrangement = Arrangement.spacedBy(ContentGap),
        ) {
            ExploreVenuePhoto(size = SummaryPhotoSize)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = venue.name,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    ExploreFavoriteButton(
                        isFavorite = isFavorite,
                        venueName = venue.name,
                        onToggle = onFavoriteToggle,
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close),
                            tint = Slate500,
                        )
                    }
                }
                Text(
                    text = venue.metaText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                )
                Spacer(modifier = Modifier.height(MetaGap))
                ExplorePriceDistanceRow(venue = venue)
            }
        }
    }
}
