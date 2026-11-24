package mx.virgo.app.ui.guest

import java.util.Locale

/** Categorías de la maqueta (RF-04 del spec `005`). */
internal const val CATEGORY_GARDENS = "Jardines"
internal const val CATEGORY_POOL = "Alberca"
internal const val CATEGORY_ELEGANT = "Elegante"
internal const val CATEGORY_HACIENDAS = "Haciendas"
internal const val CATEGORY_KIDS = "Infantil"

internal val ExploreCategories =
    listOf(
        CATEGORY_GARDENS,
        CATEGORY_POOL,
        CATEGORY_ELEGANT,
        CATEGORY_HACIENDAS,
        CATEGORY_KIDS,
    )

/** Prefijo de los ids de las muestras locales: no existen en el backend. */
internal const val SAMPLE_VENUE_ID_PREFIX = "sample-"

/**
 * Muestra local de salones para la pantalla de visitante y la de explorar, tomada de la
 * maqueta mientras el endpoint `GET /api/v1/venues` (spec 007) no entrega datos reales.
 */
data class VenueSample(
    val id: Int,
    val venueId: String,
    val name: String,
    val short: String,
    val area: String,
    val rating: Double,
    val capacity: Int,
    val distanceKm: Double,
    val bearing: Double,
    val priceBaseAmount: Double? = null,
    val priceUnit: String? = null,
    val categories: List<String> = emptyList(),
)

val VenueSamples: List<VenueSample> =
    listOf(
        VenueSample(
            id = 0,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}0",
            name = "Casa Oliva",
            short = "C. Oliva",
            area = "Juriquilla",
            rating = 4.9,
            capacity = 200,
            distanceKm = 1.2,
            bearing = 25.0,
            priceBaseAmount = 45000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_GARDENS),
        ),
        VenueSample(
            id = 1,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}1",
            name = "Jardín Magnolia",
            short = "J. Magnolia",
            area = "El Refugio",
            rating = 4.8,
            capacity = 160,
            distanceKm = 2.8,
            bearing = 120.0,
            priceBaseAmount = 38000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_GARDENS),
        ),
        VenueSample(
            id = 2,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}2",
            name = "Hacienda Los Arcos",
            short = "H. Los Arcos",
            area = "Centro",
            rating = 4.9,
            capacity = 280,
            distanceKm = 4.1,
            bearing = 305.0,
            priceBaseAmount = 62000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_HACIENDAS),
        ),
        VenueSample(
            id = 3,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}3",
            name = "Terraza Cielo",
            short = "T. Cielo",
            area = "Antea",
            rating = 4.7,
            capacity = 120,
            distanceKm = 3.4,
            bearing = 35.0,
            priceBaseAmount = 32000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_ELEGANT),
        ),
        VenueSample(
            id = 4,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}4",
            name = "Quinta Las Palmas",
            short = "Q. Las Palmas",
            area = "Santa Fe",
            rating = 4.6,
            capacity = 180,
            distanceKm = 5.6,
            bearing = 160.0,
            priceBaseAmount = 41000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_POOL),
        ),
        VenueSample(
            id = 5,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}5",
            name = "Brincolón Fiestas",
            short = "Brincolón",
            area = "Juriquilla",
            rating = 4.5,
            capacity = 90,
            distanceKm = 1.9,
            bearing = 250.0,
            priceBaseAmount = 14500.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_KIDS),
        ),
        VenueSample(
            id = 6,
            venueId = "${SAMPLE_VENUE_ID_PREFIX}6",
            name = "Rancho El Mezquite",
            short = "R. Mezquite",
            area = "Jurica",
            rating = 4.7,
            capacity = 350,
            distanceKm = 8.3,
            bearing = 215.0,
            priceBaseAmount = 58000.0,
            priceUnit = "day",
            categories = listOf(CATEGORY_GARDENS, CATEGORY_HACIENDAS),
        ),
    )

fun VenueSample.kmText(): String = String.format(Locale.ROOT, "%.1f", distanceKm)

fun VenueSample.ratingText(): String = String.format(Locale.ROOT, "%.1f", rating)
