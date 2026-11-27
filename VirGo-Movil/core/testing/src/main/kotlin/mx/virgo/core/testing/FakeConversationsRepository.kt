package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Conversation
import mx.virgo.core.domain.ConversationsRepository
import mx.virgo.core.domain.Message

/** Bandeja en memoria; [failure] simula el endpoint ausente o caído. */
class FakeConversationsRepository : ConversationsRepository {
    val conversations = mutableListOf<Conversation>()
    val messages = mutableMapOf<String, MutableList<Message>>()
    var failure: AppResult.Failure? = null
    var sendFailure: AppResult.Failure? = null
    val started = mutableListOf<Pair<String, String>>()

    override suspend fun list(): AppResult<List<Conversation>> = failure ?: AppResult.Success(conversations.toList())

    override suspend fun messages(conversationId: String): AppResult<List<Message>> =
        failure ?: AppResult.Success(messages[conversationId].orEmpty().toList())

    override suspend fun start(
        venueId: String,
        text: String,
    ): AppResult<Conversation> {
        started += venueId to text
        sendFailure?.let { return it }
        val conversation =
            conversations.firstOrNull { it.venueId == venueId }
                ?: sampleConversation().copy(id = "c-$venueId", venueId = venueId).also { conversations += it }
        messages.getOrPut(conversation.id) { mutableListOf() } +=
            Message("m-${text.hashCode()}", text, null, mine = true)
        return AppResult.Success(conversation)
    }

    override suspend fun send(
        conversationId: String,
        text: String,
    ): AppResult<Message> {
        sendFailure?.let { return it }
        val message = Message("m-${text.hashCode()}", text, null, mine = true)
        messages.getOrPut(conversationId) { mutableListOf() } += message
        return AppResult.Success(message)
    }
}

fun sampleConversation(): Conversation =
    Conversation(
        id = "c-1",
        venueId = "132a1f58-ebf9-4082-acdf-7241975f1811",
        venueName = "Salón Las Palmas",
        clientName = "Ana López",
        lastMessage = "¿Tienen fecha en mayo?",
        lastAt = null,
        unread = 1,
    )
