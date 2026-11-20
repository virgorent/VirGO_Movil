package mx.virgo.app.ui.guest

import mx.virgo.core.domain.Venue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal const val GUEST_CENTER_LAT = 20.6997
internal const val GUEST_CENTER_LNG = -100.4456

private const val EARTH_RADIUS_KM = 6371.0
private const val SHORT_NAME_MAX = 16
private const val FULL_CIRCLE_DEGREES = 360.0

internal fun List<Venue>.toSamples(): List<VenueSample> =
    mapIndexedNotNull { index, venue ->
        val latitude = venue.latitude ?: return@mapIndexedNotNull null
        val longitude = venue.longitude ?: return@mapIndexedNotNull null
        VenueSample(
            id = index,
            venueId = venue.id,
            name = venue.name,
            short = venue.shortName ?: venue.name.take(SHORT_NAME_MAX),
            area = venue.zone.orEmpty(),
            rating = venue.ratingAverage,
            capacity = venue.capacity,
            distanceKm = haversineKm(GUEST_CENTER_LAT, GUEST_CENTER_LNG, latitude, longitude),
            bearing = bearingDegrees(GUEST_CENTER_LAT, GUEST_CENTER_LNG, latitude, longitude),
            priceBaseAmount = venue.priceBaseAmount,
            priceUnit = venue.priceUnit,
            categories = venue.categories.map { normalizeCategory(it) },
        )
    }

/**
 * El catálogo de la API (`007-venues-catalog`) todavía no define el vocabulario de
 * `categories`; se normaliza a las etiquetas de la maqueta para que los filtros del
 * spec `005` coincidan sin importar el idioma en que llegue el dato.
 */
private val CategoryAliases =
    mapOf(
        "garden" to CATEGORY_GARDENS,
        "gardens" to CATEGORY_GARDENS,
        "pool" to CATEGORY_POOL,
        "elegant" to CATEGORY_ELEGANT,
        "hacienda" to CATEGORY_HACIENDAS,
        "haciendas" to CATEGORY_HACIENDAS,
        "kids" to CATEGORY_KIDS,
        "child" to CATEGORY_KIDS,
    )

private fun normalizeCategory(raw: String): String = CategoryAliases[raw.lowercase()] ?: raw

internal fun haversineKm(
    fromLat: Double,
    fromLng: Double,
    toLat: Double,
    toLng: Double,
): Double {
    val deltaLat = Math.toRadians(toLat - fromLat)
    val deltaLng = Math.toRadians(toLng - fromLng)
    val h =
        sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(Math.toRadians(fromLat)) * cos(Math.toRadians(toLat)) *
            sin(deltaLng / 2) * sin(deltaLng / 2)
    val clamped = h.coerceIn(0.0, 1.0)
    return 2.0 * EARTH_RADIUS_KM * atan2(sqrt(clamped), sqrt(1 - clamped))
}

internal fun bearingDegrees(
    fromLat: Double,
    fromLng: Double,
    toLat: Double,
    toLng: Double,
): Double {
    val from = Math.toRadians(fromLat)
    val to = Math.toRadians(toLat)
    val deltaLng = Math.toRadians(toLng - fromLng)
    val y = sin(deltaLng) * cos(to)
    val x = cos(from) * sin(to) - sin(from) * cos(to) * cos(deltaLng)
    return (Math.toDegrees(atan2(y, x)) + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
}
