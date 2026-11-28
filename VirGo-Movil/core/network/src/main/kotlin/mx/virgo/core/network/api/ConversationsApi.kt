package mx.virgo.core.network.api

import mx.virgo.core.network.dto.ConversationDto
import mx.virgo.core.network.dto.ConversationsResponseDto
import mx.virgo.core.network.dto.MessageDto
import mx.virgo.core.network.dto.MessagesResponseDto
import mx.virgo.core.network.dto.SendMessageDto
import mx.virgo.core.network.dto.StartConversationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Mensajes cliente ↔ salón (backend `084`–`088`, contrato §3). */
interface ConversationsApi {
    @GET("api/v1/conversations")
    suspend fun list(): ConversationsResponseDto

    @POST("api/v1/conversations")
    suspend fun start(
        @Body body: StartConversationDto,
    ): ConversationDto

    @GET("api/v1/conversations/{id}/messages")
    suspend fun messages(
        @Path("id") id: String,
    ): MessagesResponseDto

    @POST("api/v1/conversations/{id}/messages")
    suspend fun send(
        @Path("id") id: String,
        @Body body: SendMessageDto,
    ): MessageDto
}
