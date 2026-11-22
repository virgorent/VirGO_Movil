package mx.virgo.app.ui.guest

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleDark
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueRepository

private const val PHOTO_BADGE_ALPHA = 0.92f
private val RangeOptions = listOf(2, 5, 10)
private val PhotoSize = 96.dp
private val PillShape = RoundedCornerShape(percent = 50)
internal val PinCorner = 12.dp

@Composable
fun GuestHomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuestHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    GuestHomeLayout(
        modifier = modifier,
        header = { compact ->
            GuestHeader(
                count = state.nearby.size,
                radiusKm = state.radiusKm,
                onRadiusChange = viewModel::selectRadius,
                onLoginClick = onLoginClick,
                compact = compact,
            )
        },
        map = { mapModifier ->
            GuestMap(
                nearby = state.nearby,
                radiusKm = state.radiusKm,
                selectedId = state.selectedVenueId,
                onVenueClick = viewModel::selectVenue,
                onClearSelection = viewModel::clearVenue,
                modifier = mapModifier,
            )
        },
        footer = { compact ->
            val selected = state.selectedVenue
            when {
                selected != null ->
                    GuestPreviewCard(
                        venue = selected,
                        onClose = viewModel::clearVenue,
                        onSeeMore = { onRegisterClick(selected.id) },
                        compact = compact,
                    )
                state.hasError -> GuestError(onRetry = viewModel::refresh)
                else -> GuestHint(hasVenues = state.nearby.isNotEmpty())
            }
        },
    )
}

@Composable
private fun GuestHeader(
    count: Int,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    onLoginClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GuestBrand()
                GuestLogin(onClick = onLoginClick, modifier = Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GuestBrand()
                GuestLogin(onClick = onLoginClick)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GuestCard(count = count, radiusKm = radiusKm, onRadiusChange = onRadiusChange)
    }
}

@Composable
private fun GuestCard(
    count: Int,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
) {
    VirGoCard(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.guest_section_label),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text =
                        pluralStringResource(
                            id = R.plurals.guest_venues_near,
                            count = count,
                            formatArgs = arrayOf(count, radiusKm),
                        ),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.guest_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            RangePicker(radiusKm = radiusKm, onRadiusChange = onRadiusChange, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RangePicker(
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RangeOptions.forEach { km ->
            val active = km == radiusKm
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(PinCorner))
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        ).selectable(selected = active, role = Role.RadioButton) { onRadiusChange(km) }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.guest_range_option, km),
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GuestPreviewCard(
    venue: VenueSample,
    onClose: () -> Unit,
    onSeeMore: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    VirGoCard(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (compact) {
                VenuePhoto(venue = venue, compact = true)
                Spacer(modifier = Modifier.height(12.dp))
                GuestPreviewDetails(
                    venue = venue,
                    onClose = onClose,
                    onSeeMore = onSeeMore,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VenuePhoto(venue = venue)
                    GuestPreviewDetails(
                        venue = venue,
                        onClose = onClose,
                        onSeeMore = onSeeMore,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GuestPreviewLocked()
            if (venue.venueId.startsWith(SAMPLE_VENUE_ID_PREFIX)) {
                SamplePhotoCredits()
            }
        }
    }
}

@Composable
private fun GuestPreviewDetails(
    venue: VenueSample,
    onClose: () -> Unit,
    onSeeMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = venue.name,
                modifier = Modifier.weight(1f),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onClose, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.guest_preview_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text =
                stringResource(
                    R.string.guest_preview_meta,
                    venue.area,
                    venue.ratingText(),
                    venue.capacity,
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        VirGoOutlinedButton(
            onClick = onSeeMore,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.guest_preview_see_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun GuestPreviewLocked(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(R.string.guest_preview_locked),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VenuePhoto(
    venue: VenueSample,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier =
                Modifier
                    .size(PhotoSize)
                    .clip(RoundedCornerShape(PinCorner))
                    .background(
                        brush = Brush.linearGradient(colors = listOf(VirGoPurple, VirGoPurpleDark)),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            VenuePhotoContent(venue = venue, modifier = Modifier.matchParentSize())
            if (!compact) {
                VenueDistanceBadge(
                    venue = venue,
                    modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                )
            }
        }
        if (compact) {
            Text(
                text = stringResource(R.string.guest_distance_format, venue.kmText()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VenueDistanceBadge(
    venue: VenueSample,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = PillShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = PHOTO_BADGE_ALPHA),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = stringResource(R.string.guest_distance_format, venue.kmText()),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GuestHomeScreenPreview() {
    VirGoTheme {
        GuestHomeScreen(
            onLoginClick = {},
            onRegisterClick = {},
            viewModel = GuestHomeViewModel(GetVenuesUseCase(PreviewVenueRepository)),
        )
    }
}

private object PreviewVenueRepository : VenueRepository {
    override suspend fun getVenues(
        zone: String?,
        category: String?,
    ): AppResult<List<Venue>> = AppResult.Success(emptyList())
}
