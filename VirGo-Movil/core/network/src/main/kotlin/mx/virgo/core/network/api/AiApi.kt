package mx.virgo.core.network.api

import mx.virgo.core.network.dto.AssistantReplyDto
import mx.virgo.core.network.dto.AssistantRequestDto
import mx.virgo.core.network.dto.IdeasRequestDto
import mx.virgo.core.network.dto.IdeasResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/** IA del backend (`012`, `013`). Contrato en `docs/contrato-apis-pendientes.md`; la llave vive en el servidor. */
interface AiApi {
    @POST("api/v1/ai/ideas")
    suspend fun ideas(
        @Body body: IdeasRequestDto,
    ): IdeasResponseDto

    @POST("api/v1/assistant/messages")
    suspend fun ask(
        @Body body: AssistantRequestDto,
    ): AssistantReplyDto
}
