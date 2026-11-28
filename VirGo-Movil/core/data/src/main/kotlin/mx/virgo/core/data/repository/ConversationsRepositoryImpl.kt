package mx.virgo.core.data.repository

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Conversation
import mx.virgo.core.domain.ConversationsRepository
import mx.virgo.core.domain.Message
import mx.virgo.core.network.api.ConversationsApi
import mx.virgo.core.network.dto.ConversationDto
import mx.virgo.core.network.dto.ConversationTargetDto
import mx.virgo.core.network.dto.MessageDto
import mx.virgo.core.network.dto.SendMessageDto
import mx.virgo.core.network.dto.StartConversationDto
import java.time.Instant
import javax.inject.Inject

class ConversationsRepositoryImpl
    @Inject
    constructor(
        private val api: ConversationsApi,
    ) : ConversationsRepository {
        override suspend fun list(): AppResult<List<Conversation>> =
            contractCall {
                AppResult.Success(
                    api
                        .list()
                        .data
                        .map { it.toConversation() }
                        .sortedByDescending { it.lastAt },
                )
            }

        override suspend fun messages(conversationId: String): AppResult<List<Message>> =
            contractCall {
                AppResult.Success(
                    api
                        .messages(conversationId)
                        .data
                        .map { it.toMessage() }
                        .sortedBy { it.sentAt },
                )
            }

        override suspend fun start(
            venueId: String,
            text: String,
        ): AppResult<Conversation> =
            contractCall {
                AppResult.Success(
                    api
                        .start(
                            StartConversationDto(ConversationTargetDto(type = "venue", id = venueId), text),
                        ).toConversation(),
                )
            }

        override suspend fun send(
            conversationId: String,
            text: String,
        ): AppResult<Message> =
            contractCall { AppResult.Success(api.send(conversationId, SendMessageDto(text)).toMessage()) }
    }

private fun ConversationDto.toConversation(): Conversation =
    Conversation(
        id = id,
        venueId = target.id,
        venueName = target.name.orEmpty(),
        clientName = clientName.orEmpty(),
        lastMessage = lastMessage.orEmpty(),
        lastAt = lastAt.toInstantOrNull(),
        unread = unread.coerceAtLeast(0),
    )

private fun MessageDto.toMessage(): Message = Message(id, text, sentAt.toInstantOrNull(), mine)

private fun String?.toInstantOrNull(): Instant? = this?.let { runCatching { Instant.parse(it) }.getOrNull() }
