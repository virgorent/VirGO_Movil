package mx.virgo.core.network.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/** `type` siempre se envía: el servidor lo exige aunque la app solo hable con salones. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ConversationTargetDto(
    @EncodeDefault val type: String = "venue",
    val id: String,
    val name: String? = null,
)

@Serializable
data class ConversationDto(
    val id: String,
    val target: ConversationTargetDto,
    val clientName: String? = null,
    val lastMessage: String? = null,
    val lastAt: String? = null,
    val unread: Int = 0,
)

@Serializable
data class ConversationsResponseDto(
    val data: List<ConversationDto> = emptyList(),
)

@Serializable
data class MessageDto(
    val id: String,
    val text: String,
    val sentAt: String? = null,
    val mine: Boolean = false,
)

@Serializable
data class MessagesResponseDto(
    val data: List<MessageDto> = emptyList(),
)

@Serializable
data class StartConversationDto(
    val target: ConversationTargetDto,
    val text: String,
)

@Serializable
data class SendMessageDto(
    val text: String,
)
