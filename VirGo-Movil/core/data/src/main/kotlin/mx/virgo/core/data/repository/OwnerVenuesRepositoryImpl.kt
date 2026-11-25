package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.OwnerVenuesRepository
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueDraft
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.network.api.OwnerVenuesApi
import mx.virgo.core.network.dto.CreateVenueRequestDto
import mx.virgo.core.network.dto.OwnerVenueDto
import mx.virgo.core.network.dto.PatchVenueRequestDto
import mx.virgo.core.network.dto.VenuePriceRequestDto
import mx.virgo.core.network.dto.VenueServiceRequestDto
import java.math.BigDecimal
import javax.inject.Inject

class OwnerVenuesRepositoryImpl
    @Inject
    constructor(
        private val api: OwnerVenuesApi,
    ) : OwnerVenuesRepository {
        override suspend fun mine(): AppResult<List<OwnerVenue>> =
            safeCall { AppResult.Success(api.mine().data.map { it.toOwnerVenue() }) }

        /**
         * Si el `POST` se queda sin respuesta (red o tiempo), el servidor pudo haberlo creado:
         * antes de devolver el error se busca en `mine` un salón idéntico (022 ADR-04).
         */
        override suspend fun create(draft: VenueDraft): AppResult<OwnerVenue> {
            val before = (mine() as? AppResult.Success)?.value?.map { it.id }?.toSet()
            val created = safeCall { AppResult.Success(api.create(draft.toCreateRequest()).toOwnerVenue()) }
            if (created !is AppResult.Failure || before == null || !created.error.isUnanswered()) return created
            val recovered =
                (mine() as? AppResult.Success)?.value?.firstOrNull { venue ->
                    venue.id !in before && venue.draft.sameIdentityAs(draft)
                }
            return recovered?.let { AppResult.Success(it) } ?: created
        }

        override suspend fun update(
            id: String,
            draft: VenueDraft,
            includeServices: Boolean,
        ): AppResult<OwnerVenue> =
            safeCall {
                AppResult.Success(api.update(id, draft.toPatchRequest(includeServices)).toOwnerVenue())
            }

        override suspend fun publish(id: String): AppResult<VenueStatus> =
            safeCall { AppResult.Success(VenueStatus.fromWire(api.publish(id).status)) }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> =
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

private fun AppError.isUnanswered(): Boolean = this == AppError.Network || this == AppError.Timeout

private fun VenueDraft.sameIdentityAs(other: VenueDraft): Boolean =
    name.trim() == other.name.trim() &&
        shortName.trim() == other.shortName.trim() &&
        address.trim() == other.address.trim()

internal fun OwnerVenueDto.toOwnerVenue(): OwnerVenue =
    OwnerVenue(
        id = id,
        draft =
            VenueDraft(
                name = name,
                shortName = shortName.orEmpty(),
                description = description.orEmpty(),
                address = address.orEmpty(),
                zone = zone,
                capacity = capacity,
                priceBase = price?.baseAmount?.let { BigDecimal.valueOf(it) },
                priceUnit = PriceUnit.fromWire(price?.unit),
                minHours = minimumHours,
                categories = categories.toSet(),
                amenities = amenities.toSet(),
            ),
        status = VenueStatus.fromWire(status),
    )

private fun VenueDraft.priceRequest(): VenuePriceRequestDto =
    VenuePriceRequestDto(
        base = requireNotNull(priceBase).toDouble(),
        unit = priceUnit.wire,
        minHours = requireNotNull(minHours),
    )

private fun VenueDraft.serviceRequests(): List<VenueServiceRequestDto> =
    services.map { VenueServiceRequestDto(name = it.trim(), included = true, price = 0.0) }

internal fun VenueDraft.toCreateRequest(): CreateVenueRequestDto =
    CreateVenueRequestDto(
        name = name.trim(),
        shortName = shortName.trim(),
        description = description.trim(),
        address = address.trim(),
        zone = requireNotNull(zone),
        capacity = requireNotNull(capacity),
        price = priceRequest(),
        services = serviceRequests(),
        categories = categories.toList(),
        amenities = amenities.toList(),
    )

internal fun VenueDraft.toPatchRequest(includeServices: Boolean): PatchVenueRequestDto =
    PatchVenueRequestDto(
        name = name.trim(),
        shortName = shortName.trim(),
        description = description.trim(),
        address = address.trim(),
        zone = zone,
        capacity = capacity,
        price = if (priceBase != null && minHours != null) priceRequest() else null,
        services = if (includeServices) serviceRequests() else null,
        categories = categories.toList(),
        amenities = amenities.toList(),
    )
