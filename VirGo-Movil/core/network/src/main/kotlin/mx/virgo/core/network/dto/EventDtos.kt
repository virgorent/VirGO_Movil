package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

/** Cuerpo de crear y editar: el `PATCH` exige todos los campos; `venueId` se omite si es nulo. */
@Serializable
data class EventRequestDto(
    val name: String,
    val eventType: String,
    val date: String,
    val guests: Int,
    val budget: Double,
    val hours: Int,
    val venueId: String? = null,
)

@Serializable
data class EventVenueDto(
    val venueId: String? = null,
    val name: String? = null,
)

@Serializable
data class EventsResponseDto(
    val data: List<EventDto> = emptyList(),
)

/** Solo lo que muestra la app; diseño, plan de presupuesto e invitación llegan nulos. */
@Serializable
data class EventDto(
    val id: String,
    val name: String? = null,
    val eventType: String? = null,
    val date: String? = null,
    val guests: Int? = null,
    val budget: Double? = null,
    val hours: Int? = null,
    val venue: EventVenueDto? = null,
)

@Serializable
data class EventGuestRequestDto(
    val name: String,
    val phone: String,
    val companions: Int,
    val status: String,
    val table: String? = null,
)

@Serializable
data class EventGuestsResponseDto(
    val data: List<EventGuestDto> = emptyList(),
)

@Serializable
data class EventGuestDto(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
    val companions: Int? = null,
    val status: String? = null,
    val table: String? = null,
)

@Serializable
data class EventTaskRequestDto(
    val title: String,
    val dueDate: String,
    val done: Boolean,
)

@Serializable
data class EventTasksResponseDto(
    val data: List<EventTaskDto> = emptyList(),
)

@Serializable
data class EventTaskDto(
    val id: String,
    val title: String? = null,
    val dueDate: String? = null,
    val done: Boolean? = null,
)
