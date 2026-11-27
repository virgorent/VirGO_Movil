package mx.virgo.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.VenuePhotoContent
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.app.ui.guest.kmText
import mx.virgo.app.ui.guest.ratingText
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleDark
import mx.virgo.core.designsystem.theme.WarningYellow
import java.util.Locale

private const val BADGE_ALPHA = 0.92f
private const val PRICE_DAY = "day"
private const val PRICE_HOUR = "hour"

private val PhotoSize = 72.dp
private val StarSize = 14.dp
private val DistanceIconSize = 12.dp
private val BadgeHPadding = 6.dp
private val BadgeVPadding = 3.dp
private val BadgeGap = 4.dp
private val PhotoShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(percent = 50)

/** Foto ilustrativa para muestras y marcador neutro para salones reales sin foto. */
@Composable
internal fun ExploreVenuePhoto(
    venue: VenueSample,
    modifier: Modifier = Modifier,
    size: Dp = PhotoSize,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(PhotoShape)
                .background(
                    brush = Brush.linearGradient(colors = listOf(VirGoPurple, VirGoPurpleDark)),
                ),
        contentAlignment = Alignment.Center,
    ) {
        VenuePhotoContent(venue = venue, modifier = Modifier.matchParentSize())
    }
}

/** Precio del salón (RF-02 y RF-10); no se dibuja cuando el catálogo no trae precio. */
@Composable
internal fun ExplorePrice(
    venue: VenueSample,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Slate500,
) {
    val amount = venue.priceBaseAmount ?: return
    val formatted = String.format(Locale.ROOT, "%,.0f", amount)
    val text =
        when (venue.priceUnit) {
            PRICE_DAY -> stringResource(R.string.explore_price_day, formatted)
            PRICE_HOUR -> stringResource(R.string.explore_price_hour, formatted)
            else -> stringResource(R.string.explore_price, formatted)
        }
    Text(text = text, style = style, color = color, modifier = modifier)
}

/**
 * Favoritos apagados (`021` RF-07): el backend no tiene `/api/v1/favorites` y cada toque fallaba.
 * Se vuelve a encender cuando exista el endpoint.
 */
internal const val FAVORITES_ENABLED = false

/** Corazón de favorito (RF-08): área táctil de 48 dp con la IconButton. No se pinta sin [FAVORITES_ENABLED]. */
@Composable
internal fun ExploreFavoriteButton(
    isFavorite: Boolean,
    venueName: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = FAVORITES_ENABLED,
) {
    if (!enabled) return
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription =
                stringResource(
                    if (isFavorite) {
                        R.string.explore_favorite_remove
                    } else {
                        R.string.explore_favorite_add
                    },
                    venueName,
                ),
            tint = if (isFavorite) ErrorRed else Slate400,
        )
    }
}

/** Insignia de calificación (★ 4.9). */
@Composable
internal fun ExploreRatingPill(
    venue: VenueSample,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = PillShape,
        color = Color.White.copy(alpha = BADGE_ALPHA),
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = BadgeHPadding,
                    vertical = BadgeVPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = WarningYellow,
                modifier = Modifier.size(StarSize),
            )
            Spacer(modifier = Modifier.width(BadgeGap))
            Text(
                text = venue.ratingText(),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = Slate900,
            )
        }
    }
}

/** Precio y distancia del salón (RF-02 y RF-10), compartido por la tarjeta de lista y la resumen. */
@Composable
internal fun ExplorePriceDistanceRow(
    venue: VenueSample,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ExplorePrice(
            venue = venue,
            style = MaterialTheme.typography.labelLarge,
            color = VirGoPurple,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.Navigation,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(DistanceIconSize),
        )
        Text(
            text = stringResource(R.string.guest_distance_format, venue.kmText()),
            style = MaterialTheme.typography.labelMedium,
            color = Slate700,
        )
    }
}

/** «Zona · hasta N personas» (RF-02 y RF-10); sin la zona cuando el catálogo no la trae. */
@Composable
internal fun VenueSample.metaText(): String =
    if (area.isBlank()) {
        stringResource(R.string.explore_capacity_only, capacity)
    } else {
        stringResource(R.string.explore_venue_meta, area, capacity)
    }
