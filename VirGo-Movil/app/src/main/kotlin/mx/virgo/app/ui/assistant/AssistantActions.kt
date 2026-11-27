package mx.virgo.app.ui.assistant

/** Callbacks del chat, agrupados para probar el contenido sin `ViewModel`. */
data class AssistantActions(
    val onInput: (String) -> Unit = {},
    val onSend: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
)
