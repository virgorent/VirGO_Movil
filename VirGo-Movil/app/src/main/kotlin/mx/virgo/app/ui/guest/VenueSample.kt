package mx.virgo.app.ui.guest

import java.util.Locale

/**
 * Muestra local de salones para la pantalla de visitante, tomada de la maqueta
 * mientras el endpoint `GET /api/v1/venues` (spec 007) no entrega datos reales.
 */
data class VenueSample(
    val id: Int,
    val name: String,
    val short: String,
    val area: String,
    val rating: Double,
    val capacity: Int,
    val distanceKm: Double,
    val bearing: Double,
)

val VenueSamples: List<VenueSample> =
    listOf(
        VenueSample(
            id = 0,
            name = "Casa Oliva",
            short = "C. Oliva",
            area = "Juriquilla",
            rating = 4.9,
            capacity = 200,
            distanceKm = 1.2,
            bearing = 25.0,
        ),
        VenueSample(
            id = 1,
            name = "Jardín Magnolia",
            short = "J. Magnolia",
            area = "El Refugio",
            rating = 4.8,
            capacity = 160,
            distanceKm = 2.8,
            bearing = 120.0,
        ),
        VenueSample(
            id = 2,
            name = "Hacienda Los Arcos",
            short = "H. Los Arcos",
            area = "Centro",
            rating = 4.9,
            capacity = 280,
            distanceKm = 4.1,
            bearing = 305.0,
        ),
        VenueSample(
            id = 3,
            name = "Terraza Cielo",
            short = "T. Cielo",
            area = "Antea",
            rating = 4.7,
            capacity = 120,
            distanceKm = 3.4,
            bearing = 35.0,
        ),
        VenueSample(
            id = 4,
            name = "Quinta Las Palmas",
            short = "Q. Las Palmas",
            area = "Santa Fe",
            rating = 4.6,
            capacity = 180,
            distanceKm = 5.6,
            bearing = 160.0,
        ),
        VenueSample(
            id = 5,
            name = "Brincolón Fiestas",
            short = "Brincolón",
            area = "Juriquilla",
            rating = 4.5,
            capacity = 90,
            distanceKm = 1.9,
            bearing = 250.0,
        ),
        VenueSample(
            id = 6,
            name = "Rancho El Mezquite",
            short = "R. Mezquite",
            area = "Jurica",
            rating = 4.7,
            capacity = 350,
            distanceKm = 8.3,
            bearing = 215.0,
        ),
    )

fun VenueSample.kmText(): String = String.format(Locale.ROOT, "%.1f", distanceKm)

fun VenueSample.ratingText(): String = String.format(Locale.ROOT, "%.1f", rating)
