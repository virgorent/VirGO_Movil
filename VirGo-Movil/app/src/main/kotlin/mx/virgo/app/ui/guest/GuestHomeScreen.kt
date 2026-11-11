package mx.virgo.app.ui.guest

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.theme.Slate100
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.Slate800
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleDark
import mx.virgo.core.designsystem.theme.VirGoTheme

private const val PHOTO_ICON_ALPHA = 0.65f
private const val PHOTO_BADGE_ALPHA = 0.92f
private val RangeOptions = listOf(2, 5, 10)
private val RangeOptionWidth = 50.dp
private val RangeOptionHeight = 48.dp
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

    Box(modifier = modifier.fillMaxSize()) {
        GuestMap(
            nearby = state.nearby,
            radiusKm = state.radiusKm,
            selectedId = state.selectedVenueId,
            onVenueClick = viewModel::selectVenue,
            onClearSelection = viewModel::clearVenue,
        )
        GuestHeader(
            count = state.nearby.size,
            radiusKm = state.radiusKm,
            onRadiusChange = viewModel::selectRadius,
            onLoginClick = onLoginClick,
            modifier = Modifier.align(Alignment.TopStart),
        )
        val selected = state.selectedVenue
        if (selected != null) {
            GuestPreviewCard(
                venue = selected,
                onClose = viewModel::clearVenue,
                onSeeMore = { onRegisterClick(selected.id) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            GuestHint(
                hasVenues = state.nearby.isNotEmpty(),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun GuestHeader(
    count: Int,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = PillShape,
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Text(
                    text = stringResource(R.string.guest_brand),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = Slate900,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            VirGoOutlinedButton(onClick = onLoginClick) {
                Text(
                    text = stringResource(R.string.guest_login),
                    style = MaterialTheme.typography.labelLarge,
                    color = VirGoPurple,
                )
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
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.guest_section_label),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = Slate500,
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
                    color = Slate900,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.guest_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                    )
                }
            }

            RangePicker(radiusKm = radiusKm, onRadiusChange = onRadiusChange)
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RangeOptions.forEach { km ->
            val active = km == radiusKm
            Box(
                modifier =
                    Modifier
                        .width(RangeOptionWidth)
                        .height(RangeOptionHeight)
                        .clip(RoundedCornerShape(PinCorner))
                        .background(if (active) VirGoPurple else Slate100)
                        .clickable(role = Role.RadioButton) { onRadiusChange(km) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.guest_range_option, km),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) Color.White else Slate700,
                )
            }
        }
    }
}

@Composable
private fun GuestHint(
    hasVenues: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        shape = RoundedCornerShape(PinCorner),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = VirGoPurple,
            )
            Text(
                text =
                    stringResource(
                        if (hasVenues) R.string.guest_hint_touch else R.string.guest_hint_empty,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = Slate700,
            )
        }
    }
}

@Composable
private fun GuestPreviewCard(
    venue: VenueSample,
    onClose: () -> Unit,
    onSeeMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VirGoCard(
        containerColor = Color.White,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VenuePhoto(venue = venue)
                GuestPreviewDetails(
                    venue = venue,
                    onClose = onClose,
                    onSeeMore = onSeeMore,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GuestPreviewLocked()
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
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = Slate900,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.guest_preview_close),
                    tint = Slate500,
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
            color = Slate500,
        )

        Spacer(modifier = Modifier.height(8.dp))

        VirGoOutlinedButton(
            onClick = onSeeMore,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.guest_preview_see_more),
                style = MaterialTheme.typography.labelLarge,
                color = VirGoPurple,
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
            tint = Slate400,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(R.string.guest_preview_locked),
            style = MaterialTheme.typography.labelSmall,
            color = Slate500,
        )
    }
}

@Composable
private fun VenuePhoto(
    venue: VenueSample,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(PhotoSize)
                .clip(RoundedCornerShape(PinCorner))
                .background(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(VirGoPurple, VirGoPurpleDark),
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = Color.White.copy(alpha = PHOTO_ICON_ALPHA),
            modifier = Modifier.size(36.dp),
        )
        Surface(
            shape = PillShape,
            color = Color.White.copy(alpha = PHOTO_BADGE_ALPHA),
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = null,
                    tint = VirGoPurple,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = stringResource(R.string.guest_distance_format, venue.kmText()),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate800,
                )
            }
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
            viewModel = GuestHomeViewModel(),
        )
    }
}
