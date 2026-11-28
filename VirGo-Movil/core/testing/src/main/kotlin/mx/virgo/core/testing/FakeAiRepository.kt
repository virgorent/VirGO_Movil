package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AiRepository
import mx.virgo.core.domain.AssistantReply
import mx.virgo.core.domain.ChatMessage
import mx.virgo.core.domain.DecorIdea
import mx.virgo.core.domain.IdeasRequest

/** IA simulada: responde lo que se le asigne y guarda lo que recibió. */
class FakeAiRepository : AiRepository {
    var ideasResult: AppResult<List<DecorIdea>> = AppResult.Success(emptyList())
    var replyResult: AppResult<AssistantReply> = AppResult.Success(AssistantReply("Respuesta", uncertain = false))
    val ideaRequests = mutableListOf<IdeasRequest>()
    val questions = mutableListOf<Triple<String, String?, List<ChatMessage>>>()

    override suspend fun ideas(request: IdeasRequest): AppResult<List<DecorIdea>> {
        ideaRequests += request
        return ideasResult
    }

    override suspend fun ask(
        message: String,
        eventId: String?,
        history: List<ChatMessage>,
    ): AppResult<AssistantReply> {
        questions += Triple(message, eventId, history)
        return replyResult
    }
}
