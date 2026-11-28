package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.time.Instant

/** Conversación entre un cliente y un salón (`020`, contrato §3). */
data class Conversation(
    val id: String,
    val venueId: String,
    val venueName: String,
    val clientName: String,
    val lastMessage: String,
    val lastAt: Instant?,
    val unread: Int,
)

data class Message(
    val id: String,
    val text: String,
    val sentAt: Instant?,
    val mine: Boolean,
)

object MessageRules {
    const val MAX_LENGTH = 1000

    fun clean(text: String): String? = text.trim().takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }
}

interface ConversationsRepository {
    suspend fun list(): AppResult<List<Conversation>>

    suspend fun messages(conversationId: String): AppResult<List<Message>>

    /** Crea o recupera la conversación con el salón y manda el primer mensaje (`085`). */
    suspend fun start(
        venueId: String,
        text: String,
    ): AppResult<Conversation>

    suspend fun send(
        conversationId: String,
        text: String,
    ): AppResult<Message>
}
