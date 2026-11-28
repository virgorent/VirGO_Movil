package mx.virgo.core.data.repository

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Gig
import mx.virgo.core.domain.Provider
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderDraft
import mx.virgo.core.domain.ProviderPackage
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.network.api.ProvidersApi
import mx.virgo.core.network.dto.GigDto
import mx.virgo.core.network.dto.ProviderDto
import mx.virgo.core.network.dto.ProviderPackageDto
import mx.virgo.core.network.dto.ProviderRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Proveedores (`011`, `026`–`029`): `404`/`501` llegan como [AppError.NotFound] («llegará pronto»). */
class ProvidersRepositoryImpl
    @Inject
    constructor(
        private val api: ProvidersApi,
    ) : ProvidersRepository {
        override suspend fun catalog(category: ProviderCategory?): AppResult<List<Provider>> =
            contractCall { AppResult.Success(api.catalog(category?.wire).data.map { it.toProvider() }) }

        override suspend fun provider(id: String): AppResult<Provider> =
            contractCall { AppResult.Success(api.provider(id).toProvider()) }

        override suspend fun reviews(providerId: String): AppResult<VenueReviews> =
            contractCall {
                val dto = api.reviews(providerId)
                AppResult.Success(VenueReviews(dto.rating, dto.count, dto.data.map { it.toReview() }))
            }

        override suspend fun mine(): AppResult<Provider?> =
            when (val result = contractCall { AppResult.Success(api.mine().toProvider()) }) {
                is AppResult.Success -> result
                // Sin perfil todavía: el formulario arranca vacío y «Guardar» lo crea.
                is AppResult.Failure -> if (result.error == AppError.NotFound) AppResult.Success(null) else result
            }

        override suspend fun create(draft: ProviderDraft): AppResult<Provider> =
            contractCall { AppResult.Success(api.create(draft.toRequest()).toProvider()) }

        override suspend fun update(draft: ProviderDraft): AppResult<Provider> =
            contractCall { AppResult.Success(api.update(draft.toRequest()).toProvider()) }

        override suspend fun publish(): AppResult<Provider> =
            contractCall { AppResult.Success(api.update(ProviderRequestDto(status = PUBLISHED)).toProvider()) }

        override suspend fun gigs(): AppResult<List<Gig>> =
            contractCall { AppResult.Success(api.gigs().data.map { it.toGig() }) }

        override suspend fun saveGig(gig: Gig): AppResult<Gig> =
            contractCall {
                val body = gig.toDto()
                val saved = gig.id?.let { api.updateGig(it, body) } ?: api.createGig(body)
                AppResult.Success(saved.toGig())
            }
    }

private const val PUBLISHED = "published"

internal fun ProviderDto.toProvider(): Provider =
    Provider(
        id = id,
        businessName = businessName,
        category = ProviderCategory.fromWire(category),
        description = description.orEmpty(),
        pricing = ProviderPricing.fromWire(pricing),
        responseTime = responseTime.orEmpty(),
        packages =
            packages.map {
                ProviderPackage(it.id, it.name, it.price?.let(BigDecimal::valueOf), it.includes.orEmpty())
            },
        zones = zones,
        coverUrl = coverUrl?.takeIf { it.isNotBlank() },
        gallery = gallery.sortedBy { it.order }.map { it.url }.filter { it.isNotBlank() },
        published = status == PUBLISHED,
        ratingAverage = rating?.average ?: 0.0,
        ratingCount = rating?.count ?: 0,
    )

internal fun ProviderDraft.toRequest(): ProviderRequestDto =
    ProviderRequestDto(
        businessName = businessName.trim(),
        category = category?.wire,
        description = description.trim(),
        pricing = pricing?.wire,
        responseTime = responseTime.trim(),
        packages =
            packages.map {
                ProviderPackageDto(it.id, it.name.trim(), it.price?.toDouble(), it.includes.trim())
            },
        zones = zones.sorted(),
    )

internal fun GigDto.toGig(): Gig =
    Gig(
        id = id,
        eventName = eventName,
        clientName = clientName,
        place = place,
        date = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        start = start?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
        end = end?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
        amount = amount?.let(BigDecimal::valueOf),
        quoteId = quoteId,
    )

internal fun Gig.toDto(): GigDto =
    GigDto(
        eventName = eventName.trim(),
        clientName = clientName.trim(),
        place = place.trim(),
        date = date?.toString(),
        start = start?.toString(),
        end = end?.toString(),
        amount = amount?.toDouble(),
        quoteId = quoteId,
    )
