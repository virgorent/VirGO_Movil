package mx.virgo.core.network.api

import mx.virgo.core.network.dto.EventDto
import mx.virgo.core.network.dto.EventGuestDto
import mx.virgo.core.network.dto.EventGuestRequestDto
import mx.virgo.core.network.dto.EventGuestsResponseDto
import mx.virgo.core.network.dto.EventRequestDto
import mx.virgo.core.network.dto.EventTaskDto
import mx.virgo.core.network.dto.EventTaskRequestDto
import mx.virgo.core.network.dto.EventTasksResponseDto
import mx.virgo.core.network.dto.EventsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Eventos del cliente (backend `EventsController`). */
interface EventsApi {
    @GET("api/v1/events")
    suspend fun list(): EventsResponseDto

    @GET("api/v1/events/{id}")
    suspend fun get(
        @Path("id") id: String,
    ): EventDto

    @POST("api/v1/events")
    suspend fun create(
        @Body request: EventRequestDto,
    ): EventDto

    @PATCH("api/v1/events/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: EventRequestDto,
    ): EventDto
}

/** Invitados y pendientes de un evento. */
interface EventPlanningApi {
    @GET("api/v1/events/{id}/guests")
    suspend fun guests(
        @Path("id") eventId: String,
    ): EventGuestsResponseDto

    @POST("api/v1/events/{id}/guests")
    suspend fun createGuest(
        @Path("id") eventId: String,
        @Body request: EventGuestRequestDto,
    ): EventGuestDto

    @PATCH("api/v1/events/{id}/guests/{guestId}")
    suspend fun updateGuest(
        @Path("id") eventId: String,
        @Path("guestId") guestId: String,
        @Body request: EventGuestRequestDto,
    ): EventGuestDto

    @DELETE("api/v1/events/{id}/guests/{guestId}")
    suspend fun deleteGuest(
        @Path("id") eventId: String,
        @Path("guestId") guestId: String,
    ): Response<Unit>

    @GET("api/v1/events/{id}/tasks")
    suspend fun tasks(
        @Path("id") eventId: String,
    ): EventTasksResponseDto

    @POST("api/v1/events/{id}/tasks")
    suspend fun createTask(
        @Path("id") eventId: String,
        @Body request: EventTaskRequestDto,
    ): EventTaskDto

    @PATCH("api/v1/events/{id}/tasks/{taskId}")
    suspend fun updateTask(
        @Path("id") eventId: String,
        @Path("taskId") taskId: String,
        @Body request: EventTaskRequestDto,
    ): EventTaskDto

    @DELETE("api/v1/events/{id}/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("id") eventId: String,
        @Path("taskId") taskId: String,
    ): Response<Unit>
}
