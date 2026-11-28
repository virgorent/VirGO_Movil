package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.OwnerVenuesRepository
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueDraft
import mx.virgo.core.domain.VenueStatus
import java.math.BigDecimal

/** Repositorio en memoria: registra cada llamada para verificar que no se dupliquen salones. */
class FakeOwnerVenuesRepository : OwnerVenuesRepository {
    val venues = mutableListOf<OwnerVenue>()
    var mineFailure: AppResult.Failure? = null
    var createFailure: AppResult.Failure? = null
    var updateFailure: AppResult.Failure? = null
    var publishFailure: AppResult.Failure? = null

    var createCalls: Int = 0
        private set
    var updateCalls: Int = 0
        private set
    var publishCalls: Int = 0
        private set
    var lastIncludeServices: Boolean? = null
        private set

    override suspend fun mine(): AppResult<List<OwnerVenue>> = mineFailure ?: AppResult.Success(venues.toList())

    override suspend fun create(draft: VenueDraft): AppResult<OwnerVenue> {
        createCalls += 1
        createFailure?.let { return it }
        val venue = OwnerVenue(id = "venue-${venues.size + 1}", draft = draft, status = VenueStatus.DRAFT)
        venues += venue
        return AppResult.Success(venue)
    }

    override suspend fun update(
        id: String,
        draft: VenueDraft,
        includeServices: Boolean,
    ): AppResult<OwnerVenue> {
        updateCalls += 1
        lastIncludeServices = includeServices
        updateFailure?.let { return it }
        val index = venues.indexOfFirst { it.id == id }
        val updated = OwnerVenue(id, draft, venues.getOrNull(index)?.status ?: VenueStatus.DRAFT)
        if (index >= 0) venues[index] = updated else venues += updated
        return AppResult.Success(updated)
    }

    override suspend fun publish(id: String): AppResult<VenueStatus> {
        publishCalls += 1
        publishFailure?.let { return it }
        val index = venues.indexOfFirst { it.id == id }
        if (index >= 0) venues[index] = venues[index].copy(status = VenueStatus.PUBLISHED)
        return AppResult.Success(VenueStatus.PUBLISHED)
    }
}

fun sampleVenueDraft(): VenueDraft =
    VenueDraft(
        name = "Salón Las Palmas",
        shortName = "Las Palmas",
        description = "Salón con jardín para 200 invitados en Juriquilla.",
        address = "Av. Juriquilla 100, Querétaro",
        zone = "Juriquilla",
        capacity = 200,
        priceBase = BigDecimal("1500"),
        priceUnit = PriceUnit.HOUR,
        minHours = 4,
        categories = setOf("Salon", "Jardin"),
        amenities = setOf("Estacionamiento"),
        services = listOf("Mobiliario"),
    )
