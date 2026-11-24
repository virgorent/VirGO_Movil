package mx.virgo.app.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.GuestMap
import mx.virgo.app.ui.guest.VenueSamples

/** Original geometric illustrations: no remote photographs, permissions or image loader. */
@Composable
fun DemoVenueArtwork() {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.primaryContainer) {
        Column {
            Canvas(Modifier.fillMaxWidth().height(ArtworkHeight)) {
                drawRect(colors.primaryContainer)
                drawRoundRect(
                    colors.surface,
                    topLeft = Offset(size.width * BUILDING_LEFT, size.height * BUILDING_TOP),
                    size = Size(size.width * BUILDING_WIDTH, size.height * BUILDING_HEIGHT),
                    cornerRadius = CornerRadius(size.height * BUILDING_CORNER),
                )
                TableColumns.forEach { x ->
                    TableRows.forEach { y ->
                        val center = Offset(size.width * x, size.height * y)
                        drawCircle(colors.primary.copy(alpha = SEAT_OPACITY), size.height * SEAT_RADIUS, center)
                        drawCircle(colors.surface, size.height * TABLE_RADIUS, center)
                        drawCircle(colors.tertiary, size.height * FLOWER_RADIUS, center)
                    }
                }
                PlantColumns.forEach { x ->
                    drawCircle(
                        colors.secondary,
                        size.height * PLANT_RADIUS,
                        Offset(
                            size.width * x,
                            size.height * PLANT_TOP,
                        ),
                    )
                    drawCircle(
                        colors.tertiary,
                        size.height * PLANT_RADIUS,
                        Offset(
                            size.width * x,
                            size.height * PLANT_BOTTOM,
                        ),
                    )
                }
            }
            Text(
                stringResource(R.string.demo_artwork_caption),
                Modifier.padding(DemoMetrics.rowSpacing),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun DemoServiceArtwork() {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.tertiaryContainer) {
        Column {
            Canvas(Modifier.fillMaxWidth().height(ServiceArtworkHeight)) {
                TableColumns.forEach { x ->
                    val center = Offset(size.width * x, size.height * SERVICE_CENTER)
                    drawCircle(colors.surface, size.height * PLATE_RADIUS, center)
                    drawCircle(colors.tertiary.copy(alpha = SEAT_OPACITY), size.height * PLATE_INNER_RADIUS, center)
                    drawCircle(colors.secondary, size.height * FOOD_RADIUS, center)
                }
            }
            Text(
                stringResource(R.string.demo_service_artwork_caption),
                Modifier.padding(DemoMetrics.rowSpacing),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onTertiaryContainer,
            )
        }
    }
}

@Composable
fun DemoVenueMap(onNavigate: (String) -> Unit) {
    Surface(shape = MaterialTheme.shapes.large) {
        Column {
            GuestMap(
                nearby = VenueSamples.filter { it.distanceKm <= MAP_RADIUS },
                radiusKm = MAP_RADIUS,
                selectedId = -1,
                onVenueClick = { onNavigate("venue") },
                onClearSelection = {},
                modifier = Modifier.fillMaxWidth().height(MapHeight),
            )
            Text(
                stringResource(R.string.demo_map_caption),
                Modifier.padding(DemoMetrics.rowSpacing),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private val ArtworkHeight = 200.dp
private val ServiceArtworkHeight = 140.dp
private val MapHeight = 300.dp
private val TableColumns = listOf(0.25f, 0.5f, 0.75f)
private val TableRows = listOf(0.44f, 0.77f)
private val PlantColumns = listOf(0.06f, 0.94f)
private const val BUILDING_LEFT = 0.13f
private const val BUILDING_TOP = 0.08f
private const val BUILDING_WIDTH = 0.74f
private const val BUILDING_HEIGHT = 0.14f
private const val BUILDING_CORNER = 0.03f
private const val SEAT_OPACITY = 0.18f
private const val SEAT_RADIUS = 0.145f
private const val TABLE_RADIUS = 0.11f
private const val FLOWER_RADIUS = 0.025f
private const val PLANT_RADIUS = 0.08f
private const val PLANT_TOP = 0.3f
private const val PLANT_BOTTOM = 0.86f
private const val SERVICE_CENTER = 0.5f
private const val PLATE_RADIUS = 0.26f
private const val PLATE_INNER_RADIUS = 0.2f
private const val FOOD_RADIUS = 0.12f
private const val MAP_RADIUS = 5
