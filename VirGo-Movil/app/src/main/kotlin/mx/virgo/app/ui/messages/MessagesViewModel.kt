package mx.virgo.app.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.SessionRole
import mx.virgo.app.session.endsSession
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Conversation
import mx.virgo.core.domain.ConversationsRepository
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.Message
import mx.virgo.core.domain.MessageRules
import javax.inject.Inject

enum class InboxStatus {
    LOADING,
    READY,
    UNAVAILABLE,
    FAILED,
}

/** Conversación abierta (`020` RF-02 y RF-03). */
data class ThreadState(
    val conversation: Conversation,
    val messages: List<Message> = emptyList(),
    val loading: Boolean = true,
    val failed: Boolean = false,
)

/** «Nuevo mensaje» del cliente (`020` RF-04): salones a los que ya pidió cotización. */
data class NewConversation(
    val venues: List<Pair<String, String>> = emptyList(),
    val venueId: String? = null,
)

data class MessagesUiState(
    val status: InboxStatus = InboxStatus.LOADING,
    val conversations: List<Conversation> = emptyList(),
    val isOwner: Boolean = false,
    val thread: ThreadState? = null,
    val composing: NewConversation? = null,
    val input: String = "",
    val sending: Boolean = false,
    val sendFailed: Boolean = false,
) {
    val canSend: Boolean get() =
        !sending &&
            MessageRules.clean(input) != null &&
            (thread != null || composing?.venueId != null)
}

/** Bandeja y conversaciones con el salón (`020`), para cliente y propietario. */
@HiltViewModel
class MessagesViewModel
    @Inject
    constructor(
        private val repository: ConversationsRepository,
        private val getQuotes: GetQuotesUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui =
            MutableStateFlow(
                MessagesUiState(
                    isOwner =
                        session.state.value.session
                            ?.role == SessionRole.OWNER,
                ),
            )
        val state: StateFlow<MessagesUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(status = InboxStatus.LOADING)
            viewModelScope.launch {
                ui.value =
                    when (val result = repository.list()) {
                        is AppResult.Success -> ui.value.copy(status = InboxStatus.READY, conversations = result.value)
                        is AppResult.Failure -> ui.value.copy(status = inboxProblem(result.error) ?: return@launch)
                    }
            }
        }

        fun open(conversation: Conversation) {
            ui.value =
                ui.value.copy(thread = ThreadState(conversation), composing = null, input = "", sendFailed = false)
            viewModelScope.launch {
                val loaded =
                    when (val result = repository.messages(conversation.id)) {
                        is AppResult.Success -> ThreadState(conversation, result.value, loading = false)
                        is AppResult.Failure ->
                            if (session.endsSession(
                                    result.error,
                                )
                            ) {
                                return@launch
                            } else {
                                ThreadState(conversation, loading = false, failed = true)
                            }
                    }
                if (ui.value.thread
                        ?.conversation
                        ?.id == conversation.id
                ) {
                    ui.value = ui.value.copy(thread = loaded)
                }
            }
        }

        /** Cierra la conversación o el nuevo mensaje y vuelve a la bandeja. */
        fun close() {
            ui.value = ui.value.copy(thread = null, composing = null, input = "", sendFailed = false)
            refresh()
        }

        fun compose(venueId: String? = null) {
            val current = ui.value.composing
            if (current != null) {
                ui.value = ui.value.copy(composing = current.copy(venueId = venueId))
                return
            }
            ui.value = ui.value.copy(composing = NewConversation(), thread = null, input = "")
            viewModelScope.launch {
                val venues =
                    (getQuotes() as? AppResult.Success)
                        ?.value
                        ?.mapNotNull { quote -> quote.venueId?.let { it to quote.venueName } }
                        ?.distinctBy { it.first }
                        .orEmpty()
                ui.value = ui.value.copy(composing = ui.value.composing?.copy(venues = venues))
            }
        }

        fun changeInput(text: String) {
            ui.value = ui.value.copy(input = text.take(MessageRules.MAX_LENGTH), sendFailed = false)
        }

        fun send() {
            val current = ui.value
            val text = MessageRules.clean(current.input) ?: return
            if (!current.canSend) return
            ui.value = current.copy(sending = true, sendFailed = false)
            viewModelScope.launch {
                val thread = current.thread
                if (thread != null) deliver(thread, text) else start(requireNotNull(current.composing?.venueId), text)
            }
        }

        private suspend fun deliver(
            thread: ThreadState,
            text: String,
        ) {
            when (val result = repository.send(thread.conversation.id, text)) {
                is AppResult.Success ->
                    ui.value =
                        ui.value.copy(
                            sending = false,
                            input = "",
                            thread =
                                thread.copy(
                                    messages =
                                        thread.messages + result.value,
                                ),
                        )
                is AppResult.Failure -> sendFailed(result.error)
            }
        }

        private suspend fun start(
            venueId: String,
            text: String,
        ) {
            when (val result = repository.start(venueId, text)) {
                is AppResult.Success -> {
                    ui.value = ui.value.copy(sending = false, input = "")
                    open(result.value)
                }
                is AppResult.Failure -> sendFailed(result.error)
            }
        }

        private suspend fun sendFailed(error: AppError) {
            if (session.endsSession(error)) return
            ui.value = ui.value.copy(sending = false, sendFailed = true)
        }

        private suspend fun inboxProblem(error: AppError): InboxStatus? =
            when {
                session.endsSession(error) -> null
                error == AppError.NotFound -> InboxStatus.UNAVAILABLE
                else -> InboxStatus.FAILED
            }
    }
