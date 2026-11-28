package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal

/** Unidad de cobro del salón; `wire` es el valor exacto del servidor. */
enum class PriceUnit(
    val wire: String,
) {
    HOUR("hour"),
    DAY("day"),
    ;

    companion object {
        fun fromWire(value: String?): PriceUnit = entries.firstOrNull { it.wire == value } ?: HOUR
    }
}

/** Estado de publicación; un valor desconocido se trata como «En revisión» (022 RF-01). */
enum class VenueStatus {
    DRAFT,
    REVIEW,
    PUBLISHED,
    ;

    companion object {
        fun fromWire(value: String?): VenueStatus =
            when (value) {
                "draft" -> DRAFT
                "published" -> PUBLISHED
                else -> REVIEW
            }
    }
}

/** Listas cerradas del servidor (`VenuesController`); se envían tal cual. */
object VenueCatalogs {
    val zones =
        listOf("Juriquilla", "Centro", "El Refugio", "Santa Fe", "Antea", "Jurica", "Corregidora", "El Marqués")
    val categories = listOf("Salon", "Jardin", "Terraza", "Hacienda", "Rooftop", "Salon de eventos")
    val amenities =
        listOf("Estacionamiento", "Cocina", "Aire acondicionado", "Pista de baile", "Sonido", "Pantalla LED")
}

/**
 * Datos capturados en el asistente. Los numéricos son `null` cuando el texto no es un número
 * válido. `services` son nombres de servicios incluidos (sin precio en el MVP).
 */
data class VenueDraft(
    val name: String = "",
    val shortName: String = "",
    val description: String = "",
    val address: String = "",
    val zone: String? = null,
    val capacity: Int? = null,
    val priceBase: BigDecimal? = null,
    val priceUnit: PriceUnit = PriceUnit.HOUR,
    val minHours: Int? = null,
    val categories: Set<String> = emptySet(),
    val amenities: Set<String> = emptySet(),
    val services: List<String> = emptyList(),
)

data class OwnerVenue(
    val id: String,
    val draft: VenueDraft,
    val status: VenueStatus,
    val photos: List<String> = emptyList(),
)

interface OwnerVenuesRepository {
    suspend fun mine(): AppResult<List<OwnerVenue>>

    suspend fun create(draft: VenueDraft): AppResult<OwnerVenue>

    /** `includeServices = false` omite los servicios (al retomar, `mine` no los devuelve). */
    suspend fun update(
        id: String,
        draft: VenueDraft,
        includeServices: Boolean,
    ): AppResult<OwnerVenue>

    suspend fun publish(id: String): AppResult<VenueStatus>
}
