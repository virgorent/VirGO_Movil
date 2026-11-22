package mx.virgo.app.ui.guest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import kotlin.math.cos
import kotlin.math.sin

private const val MIN_TARGET_DP = 48
private const val CENTER_FRACTION = 0.5f
private const val RANGE_FILL_ALPHA = 0.10f
private const val DIAGONAL_START = 0.85f
private const val DIAGONAL_END = 0.15f

private val RangeRadius = 150.dp
private val RangeStrokeWidth = 3.dp
private val RangeLabelGap = 20.dp
private val DashPattern = floatArrayOf(16f, 12f)
private val RoadWidth = 14.dp
private val HorizontalRoadFractions = listOf(0.28f, 0.55f, 0.78f)
private val VerticalRoadFractions = listOf(0.22f, 0.5f, 0.82f)
private val UserMarkerSize = 40.dp
private val UserMarkerRadius = 20.dp
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
    val colors = MaterialTheme.colorScheme
    val labelSizes = measurePinLabels(nearby)
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rangeRadius = minOf(RangeRadius, (maxWidth - PinTargetSize) / 2)
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(radiusKm) { detectTapGestures { onClearSelection() } },
        ) {
            drawMap(
                center = Offset(x = size.width * CENTER_FRACTION, y = size.height * CENTER_FRACTION),
                colors = colors,
                rangeRadius = rangeRadius,
            )
        }

        val pins =
            computePins(
                nearby = nearby,
                selectedId = selectedId,
                pxPerKm = rangeRadius / radiusKm,
                labelSizes = labelSizes,
                mapSize = DpSize(maxWidth, maxHeight),
            )

        Text(
            text = stringResource(R.string.guest_range_option, radiusKm),
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = -(rangeRadius + RangeLabelGap)),
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

private fun DrawScope.drawMap(
    center: Offset,
    colors: ColorScheme,
    rangeRadius: Dp,
) {
    drawRect(
        brush =
            Brush.verticalGradient(
                colors = listOf(colors.primaryContainer, colors.background),
            ),
    )
    HorizontalRoadFractions.forEach { fraction ->
        drawLine(
            color = colors.outlineVariant,
            start = Offset(x = 0f, y = size.height * fraction),
            end = Offset(x = size.width, y = size.height * fraction),
            strokeWidth = RoadWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
    VerticalRoadFractions.forEach { fraction ->
        drawLine(
            color = colors.outlineVariant,
            start = Offset(x = size.width * fraction, y = 0f),
            end = Offset(x = size.width * fraction, y = size.height),
            strokeWidth = RoadWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
    drawLine(
        color = colors.outlineVariant,
        start = Offset(x = 0f, y = size.height * DIAGONAL_START),
        end = Offset(x = size.width, y = size.height * DIAGONAL_END),
        strokeWidth = RoadWidth.toPx(),
        cap = StrokeCap.Round,
    )
    drawCircle(
        color = colors.primary.copy(alpha = RANGE_FILL_ALPHA),
        radius = rangeRadius.toPx(),
        center = center,
    )
    drawCircle(
        color = colors.primary,
        radius = rangeRadius.toPx(),
        center = center,
        style =
            Stroke(
                width = RangeStrokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(DashPattern),
            ),
    )
}

@Composable
private fun measurePinLabels(nearby: List<VenueSample>): Map<Int, DpSize> {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    val distanceStyle = MaterialTheme.typography.labelSmall
    return nearby.associate { venue ->
        val name = measurer.measure(AnnotatedString(venue.short), style = nameStyle).size
        val distance =
            measurer
                .measure(
                    AnnotatedString(stringResource(R.string.guest_distance_format, venue.kmText())),
                    style = distanceStyle,
                ).size
        venue.id to
            with(density) {
                DpSize(
                    width = maxOf(PinTargetSize, maxOf(name.width, distance.width).toDp() + 20.dp),
                    height = maxOf(PinTargetSize, (name.height + distance.height).toDp() + 16.dp),
                )
            }
    }
}

private fun computePins(
    nearby: List<VenueSample>,
    selectedId: Int,
    pxPerKm: Dp,
    labelSizes: Map<Int, DpSize>,
    mapSize: DpSize,
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
            val size = labelSizes.getValue(venue.id)
            val half = size.width / 2
            val halfHeight = size.height / 2
            val xLimit = ((mapSize.width - size.width) / 2).coerceAtLeast(0.dp)
            val yLimit = ((mapSize.height - size.height) / 2).coerceAtLeast(0.dp)
            val originalX = (pxPerKm.value * venue.distanceKm * sin(angle)).toFloat().dp
            val originalY = (-pxPerKm.value * venue.distanceKm * cos(angle)).toFloat().dp
            val x = originalX.coerceIn(-xLimit, xLimit)
            val y = originalY.coerceIn(-yLimit, yLimit)
            val box =
                LabelBox(
                    left = x - half,
                    top = y - halfHeight,
                    right = x + half,
                    bottom = y + halfHeight,
                )
            val labelled =
                size.width <= mapSize.width &&
                    size.height <= mapSize.height &&
                    placed.none { it.overlaps(box) }
            if (labelled) {
                placed += box
                PinPlacement(venue = venue, x = x, y = y, compact = false)
            } else {
                val compactXLimit = ((mapSize.width - PinTargetSize) / 2).coerceAtLeast(0.dp)
                val compactYLimit = ((mapSize.height - PinTargetSize) / 2).coerceAtLeast(0.dp)
                val compactX = originalX.coerceIn(-compactXLimit, compactXLimit)
                val compactY = originalY.coerceIn(-compactYLimit, compactYLimit)
                val halfTarget = PinTargetSize / 2
                placed +=
                    LabelBox(
                        left = compactX - halfTarget,
                        top = compactY - halfTarget,
                        right = compactX + halfTarget,
                        bottom = compactY + halfTarget,
                    )
                PinPlacement(venue = venue, x = compactX, y = compactY, compact = true)
            }
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
                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = stringResource(R.string.guest_pin_cd, pin.venue.name, pin.venue.kmText()),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
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
    val description = stringResource(R.string.guest_pin_cd, pin.venue.name, pin.venue.kmText())
    Surface(
        modifier =
            modifier
                .defaultMinSize(minWidth = PinTargetSize, minHeight = PinTargetSize)
                .semantics { contentDescription = description }
                .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(PinCorner),
        color = MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.guest_distance_format, pin.venue.kmText()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .background(MaterialTheme.colorScheme.primary)
                .border(width = 3.dp, color = MaterialTheme.colorScheme.onPrimary, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = stringResource(R.string.guest_you_here),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}
