package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

/**
 * Detalle público de un salón (006). `priceBase` es `null` sin sesión (regla A3) o si el
 * servidor no lo envía; la app nunca lo inventa.
 */
data class VenueDetail(
    val id: String,
    val name: String,
    val description: String?,
    val address: String?,
    val zone: String?,
    val capacity: Int,
    val minimumHours: Int?,
    val priceBase: Double?,
    val priceUnit: PriceUnit?,
    val categories: List<String>,
    val amenities: List<String>,
    val ratingAverage: Double,
    val ratingCount: Int,
    val verified: Boolean,
)

interface VenueDetailRepository {
    suspend fun venue(id: String): AppResult<VenueDetail>
}

class GetVenueDetailUseCase
    @Inject
    constructor(
        private val repository: VenueDetailRepository,
    ) {
        suspend operator fun invoke(id: String): AppResult<VenueDetail> = repository.venue(id)
    }
