package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

/** `type` no lleva valor por omisión: con `encodeDefaults=false` se omitiría. */
@Serializable
data class QuoteTargetDto(
    val type: String,
    val id: String,
)

/** Sin `eventId` (008 ADR-02) ni `clientName`/`target.name`: los resuelve el servidor. */
@Serializable
data class CreateQuoteRequestDto(
    val target: QuoteTargetDto,
    val eventType: String,
    val eventDate: String,
    val guests: Int,
    val message: String? = null,
)

@Serializable
data class QuotesResponseDto(
    val data: List<QuoteDto> = emptyList(),
)

@Serializable
data class QuoteResponseDto(
    val total: Double? = null,
    val deposit: Double? = null,
    val message: String? = null,
)

/** Solo los campos que muestra la app; el cliente y los servicios se ignoran. */
@Serializable
data class QuoteDto(
    val id: String,
    val targetId: String? = null,
    val targetName: String? = null,
    val eventType: String? = null,
    val eventDate: String? = null,
    val guests: Int? = null,
    val message: String? = null,
    val status: String? = null,
    val response: QuoteResponseDto? = null,
    val createdAt: String? = null,
)
