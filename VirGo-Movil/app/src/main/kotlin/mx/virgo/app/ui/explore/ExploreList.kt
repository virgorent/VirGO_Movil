package mx.virgo.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.SAMPLE_VENUE_ID_PREFIX
import mx.virgo.app.ui.guest.SamplePhotoCredits
import mx.virgo.app.ui.guest.VenuePhotoContent
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurpleLight

private val ListHPadding = 16.dp
private val ListVPadding = 8.dp
private val CardGap = 12.dp
private val CardPadding = 12.dp
private val InnerGap = 4.dp
private val PhotoHeight = 164.dp
private val StateIconSize = 48.dp
private val StateGap = 12.dp
private val RetryGap = 16.dp
private val CardShape = RoundedCornerShape(24.dp)

/** C9: lista de salones con orden por recomendado, cercanÃ­a, precio o calificaciÃ³n (RF-11). */
@Composable
internal fun ExploreListContent(
    state: ExploreUiState,
    onOrderChange: (ExploreOrder) -> Unit,
    onVenueClick: (Int) -> Unit,
    onFavoriteToggle: (VenueSample) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ExploreSortRow(order = state.filters.order, onOrderChange = onOrderChange)
        when {
            state.hasError ->
                ExploreErrorState(
                    onRetry = onRetry,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            state.results.isEmpty() ->
                ExploreEmptyState(modifier = Modifier.weight(1f).fillMaxWidth())
            else ->
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding =
                        PaddingValues(
                            horizontal = ListHPadding,
                            vertical = ListVPadding,
                        ),
                    verticalArrangement = Arrangement.spacedBy(CardGap),
                ) {
                    items(items = state.results, key = { it.id }) { venue ->
                        ExploreVenueCard(
                            venue = venue,
                            isFavorite = venue.venueId in state.favoriteIds,
                            onClick = { onVenueClick(venue.id) },
                            onFavoriteToggle = { onFavoriteToggle(venue) },
                        )
                    }
                    if (state.results.any { it.venueId.startsWith(SAMPLE_VENUE_ID_PREFIX) }) {
                        item { SamplePhotoCredits() }
                    }
                }
        }
    }
}

@Composable
private fun ExploreSortRow(
    order: ExploreOrder,
    onOrderChange: (ExploreOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ListHPadding),
        horizontalArrangement = Arrangement.spacedBy(CardGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.explore_order_cd),
            style = MaterialTheme.typography.labelMedium,
            color = Slate500,
        )
        ExploreOrders.forEach { option ->
            ExploreChip(
                label = option.label(),
                selected = option == order,
                onClick = { onOrderChange(option) },
            )
        }
    }
}

/** RF-10: tarjeta con foto, calificaciÃ³n, capacidad, precio y distancia. */
@Composable
private fun ExploreVenueCard(
    venue: VenueSample,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VirGoCard(
        containerColor = Color.White,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(CardShape)
                .clickable(onClick = onClick),
    ) {
        Column {
            ExploreCardPhoto(venue = venue)
            Column(modifier = Modifier.padding(CardPadding)) {
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
                }
                Text(
                    text = venue.metaText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                )
                Spacer(modifier = Modifier.height(InnerGap))
                ExplorePriceDistanceRow(venue = venue)
            }
        }
    }
}

@Composable
private fun ExploreCardPhoto(venue: VenueSample) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(PhotoHeight)
                .background(VirGoPurpleLight),
    ) {
        VenuePhotoContent(venue = venue, modifier = Modifier.matchParentSize())
        ExploreRatingPill(
            venue = venue,
            modifier = Modifier.align(Alignment.BottomStart).padding(CardPadding),
        )
        if (venue.venueId.startsWith(SAMPLE_VENUE_ID_PREFIX)) {
            Surface(
                color = Color.White.copy(alpha = 0.94f),
                shape = CardShape,
                modifier = Modifier.align(Alignment.BottomEnd).padding(CardPadding),
            ) {
                Text(
                    text = stringResource(R.string.explore_sample_image),
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate900,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/** RF-19: avisa cuando ningÃºn salÃ³n cumple con todos los filtros. */
@Composable
internal fun ExploreEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ListHPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(StateIconSize),
        )
        Spacer(modifier = Modifier.height(StateGap))
        Text(
            text = stringResource(R.string.explore_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center,
        )
    }
}

/** CA-05 y CA-06: error de la API o sin conexiÃ³n, con opciÃ³n de reintentar. */
@Composable
internal fun ExploreErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ListHPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(StateIconSize),
        )
        Spacer(modifier = Modifier.height(StateGap))
        Text(
            text = stringResource(R.string.guest_error_load),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RetryGap))
        VirGoButton(
            onClick = onRetry,
            text = stringResource(R.string.guest_retry),
        )
    }
}
