package mx.virgo.app.ui.guest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.Slate200
import mx.virgo.core.designsystem.theme.Slate50
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleDark
import mx.virgo.core.designsystem.theme.VirGoPurpleLight
import kotlin.math.cos
import kotlin.math.sin

private const val LABEL_CHAR_WIDTH = 3
private const val LABEL_SIDE_PADDING = 20
private const val MIN_TARGET_DP = 48
private const val CENTER_FRACTION = 0.5f
private const val RANGE_FILL_ALPHA = 0.10f
private const val RANGE_STROKE_ALPHA = 0.55f
private const val DIAGONAL_START = 0.85f
private const val DIAGONAL_END = 0.15f

private val RangeRadius = 150.dp
private val RangeStrokeWidth = 3.dp
private val RangeLabelGap = 20.dp
private val DashPattern = floatArrayOf(16f, 12f)
private val RoadColor = Slate200
private val RoadWidth = 14.dp
private val HorizontalRoadFractions = listOf(0.28f, 0.55f, 0.78f)
private val VerticalRoadFractions = listOf(0.22f, 0.5f, 0.82f)
private val MapBackgroundTop = VirGoPurpleLight
private val MapBackgroundBottom = Slate50
private val UserMarkerSize = 40.dp
private val UserMarkerRadius = 20.dp
private val LabelHalfHeight = 20.dp
private val PinTargetSize = MIN_TARGET_DP.dp

private data class LabelBox(
    val left: Dp,
    val top: Dp,
    val right: Dp,
    val bottom: Dp,
) {
    fun overlaps(other: LabelBox): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom
}

private data class PinPlacement(
    val venue: VenueSample,
    val x: Dp,
    val y: Dp,
    val compact: Boolean,
)

@Composable
internal fun GuestMap(
    nearby: List<VenueSample>,
    radiusKm: Int,
    selectedId: Int,
    onVenueClick: (Int) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(radiusKm) { detectTapGestures { onClearSelection() } },
        ) {
            drawMap(center = Offset(x = size.width * CENTER_FRACTION, y = size.height * CENTER_FRACTION))
        }

        val pins = computePins(nearby = nearby, selectedId = selectedId, pxPerKm = RangeRadius / radiusKm)

        Text(
            text = stringResource(R.string.guest_range_option, radiusKm),
            style = MaterialTheme.typography.labelMedium,
            color = VirGoPurpleDark,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = -(RangeRadius + RangeLabelGap)),
        )

        UserLocationMarker(
            modifier = Modifier.align(Alignment.Center),
        )

        pins.forEach { pin ->
            VenuePin(
                pin = pin,
                selected = pin.venue.id == selectedId,
                onClick = { onVenueClick(pin.venue.id) },
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .offset(x = pin.x, y = pin.y),
            )
        }
    }
}

private fun DrawScope.drawMap(center: Offset) {
    drawRect(
        brush =
            Brush.verticalGradient(
                colors = listOf(MapBackgroundTop, MapBackgroundBottom),
            ),
    )
    HorizontalRoadFractions.forEach { fraction ->
        drawLine(
            color = RoadColor,
            start = Offset(x = 0f, y = size.height * fraction),
            end = Offset(x = size.width, y = size.height * fraction),
            strokeWidth = RoadWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
    VerticalRoadFractions.forEach { fraction ->
        drawLine(
            color = RoadColor,
            start = Offset(x = size.width * fraction, y = 0f),
            end = Offset(x = size.width * fraction, y = size.height),
            strokeWidth = RoadWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
    drawLine(
        color = RoadColor,
        start = Offset(x = 0f, y = size.height * DIAGONAL_START),
        end = Offset(x = size.width, y = size.height * DIAGONAL_END),
        strokeWidth = RoadWidth.toPx(),
        cap = StrokeCap.Round,
    )
    drawCircle(
        color = VirGoPurple.copy(alpha = RANGE_FILL_ALPHA),
        radius = RangeRadius.toPx(),
        center = center,
    )
    drawCircle(
        color = VirGoPurple.copy(alpha = RANGE_STROKE_ALPHA),
        radius = RangeRadius.toPx(),
        center = center,
        style =
            Stroke(
                width = RangeStrokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(DashPattern),
            ),
    )
}

private fun computePins(
    nearby: List<VenueSample>,
    selectedId: Int,
    pxPerKm: Dp,
): List<PinPlacement> {
    val placed =
        mutableListOf(
            LabelBox(
                left = -UserMarkerRadius,
                top = -UserMarkerRadius,
                right = UserMarkerRadius,
                bottom = UserMarkerRadius,
            ),
        )
    return nearby
        .sortedByDescending { it.id == selectedId }
        .map { venue ->
            val angle = Math.toRadians(venue.bearing)
            val x = (pxPerKm.value * venue.distanceKm * sin(angle)).toFloat().dp
            val y = (-pxPerKm.value * venue.distanceKm * cos(angle)).toFloat().dp
            val half = ((venue.short.length * LABEL_CHAR_WIDTH) + LABEL_SIDE_PADDING).dp
            val box =
                LabelBox(
                    left = x - half,
                    top = y - LabelHalfHeight,
                    right = x + half,
                    bottom = y + LabelHalfHeight,
                )
            val labelled = venue.id == selectedId || placed.none { it.overlaps(box) }
            if (labelled) placed += box
            PinPlacement(venue = venue, x = x, y = y, compact = !labelled)
        }
}

@Composable
private fun VenuePin(
    pin: PinPlacement,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pin.compact) {
        CompactPin(pin = pin, selected = selected, onClick = onClick, modifier = modifier)
    } else {
        LabelledPin(pin = pin, selected = selected, onClick = onClick, modifier = modifier)
    }
}

@Composable
private fun CompactPin(
    pin: PinPlacement,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(PinTargetSize)
                .clip(CircleShape)
                .background(if (selected) VirGoPurple else Color.White)
                .border(
                    width = 1.dp,
                    color = if (selected) VirGoPurple else Slate200,
                    shape = CircleShape,
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = stringResource(R.string.guest_pin_cd, pin.venue.name, pin.venue.kmText()),
            tint = if (selected) Color.White else VirGoPurple,
            modifier = Modifier.size(PinTargetSize / 2),
        )
    }
}

@Composable
private fun LabelledPin(
    pin: PinPlacement,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .defaultMinSize(minWidth = PinTargetSize, minHeight = PinTargetSize)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(PinCorner),
        color = Color.White,
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) VirGoPurple else Slate200,
            ),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = pin.venue.short,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.guest_distance_format, pin.venue.kmText()),
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun UserLocationMarker(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(UserMarkerSize)
                .clip(CircleShape)
                .background(VirGoPurple)
                .border(width = 3.dp, color = Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = stringResource(R.string.guest_you_here),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
