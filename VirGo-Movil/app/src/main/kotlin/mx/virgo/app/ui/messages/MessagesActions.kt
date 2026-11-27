package mx.virgo.app.ui.messages

import mx.virgo.core.domain.Conversation

/** Callbacks de «Mensajes», agrupados para probar el contenido sin `ViewModel`. */
data class MessagesActions(
    val onOpen: (Conversation) -> Unit = {},
    val onCompose: () -> Unit = {},
    val onVenue: (String) -> Unit = {},
    val onInput: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onRetry: () -> Unit = {},
)
