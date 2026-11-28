package mx.virgo.core.data.repository

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AiRepository
import mx.virgo.core.domain.AssistantReply
import mx.virgo.core.domain.ChatMessage
import mx.virgo.core.domain.DecorIdea
import mx.virgo.core.domain.IdeasRequest
import mx.virgo.core.network.api.AiApi
import mx.virgo.core.network.dto.AssistantRequestDto
import mx.virgo.core.network.dto.ChatTurnDto
import mx.virgo.core.network.dto.IdeasRequestDto
import java.math.BigDecimal
import javax.inject.Inject

class AiRepositoryImpl
    @Inject
    constructor(
        private val api: AiApi,
    ) : AiRepository {
        override suspend fun ideas(request: IdeasRequest): AppResult<List<DecorIdea>> =
            contractCall {
                val body = IdeasRequestDto(request.eventType?.wire, request.guests, request.budget?.toDouble())
                val ideas =
                    api.ideas(body).data.filter { it.title.isNotBlank() }.map {
                        DecorIdea(
                            it.title.trim(),
                            it.description?.takeIf(String::isNotBlank),
                            BigDecimal.valueOf(it.cost),
                        )
                    }
                AppResult.Success(ideas)
            }

        override suspend fun ask(
            message: String,
            eventId: String?,
            history: List<ChatMessage>,
        ): AppResult<AssistantReply> =
            contractCall {
                val turns = history.map { ChatTurnDto(if (it.fromUser) "user" else "assistant", it.text) }
                val reply = api.ask(AssistantRequestDto(message, eventId, turns))
                AppResult.Success(AssistantReply(reply.reply.trim(), reply.uncertain))
            }
    }
