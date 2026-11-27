package mx.virgo.app.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.endsSession
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AskAssistantUseCase
import mx.virgo.core.domain.ChatMessage
import mx.virgo.core.domain.EventsRepository
import javax.inject.Inject

/** Por qué no llegó la respuesta (`013` RF-06). */
enum class AssistantProblem {
    UNAVAILABLE,
    BUSY,
    FAILED,
}

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val sending: Boolean = false,
    val problem: AssistantProblem? = null,
    /** Pregunta que falló y se puede reintentar. */
    val pending: String? = null,
) {
    val canSend: Boolean get() = !sending && input.isNotBlank()
}

/** Chat con la IA del servidor (`013`); la conversación no se guarda en el dispositivo (RF-07). */
@HiltViewModel
class AssistantViewModel
    @Inject
    constructor(
        private val ask: AskAssistantUseCase,
        private val events: EventsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(AssistantUiState())
        val state: StateFlow<AssistantUiState> = ui.asStateFlow()
        private var eventId: String? = null
        private var eventLoaded = false

        fun changeInput(text: String) {
            ui.value = ui.value.copy(input = text.take(AskAssistantUseCase.MAX_LENGTH))
        }

        fun send(text: String = ui.value.input) {
            val question = text.trim()
            if (question.isEmpty() || ui.value.sending) return
            val history = ui.value.messages
            ui.value =
                ui.value.copy(
                    messages = history + ChatMessage(question, fromUser = true),
                    input = "",
                    sending = true,
                    problem = null,
                    pending = null,
                )
            viewModelScope.launch { deliver(question, history) }
        }

        /** Vuelve a mandar la pregunta que falló, sin repetirla en la lista. */
        fun retry() {
            val question = ui.value.pending ?: return
            ui.value = ui.value.copy(sending = true, problem = null, pending = null)
            viewModelScope.launch { deliver(question, ui.value.messages.dropLast(1)) }
        }

        private suspend fun deliver(
            question: String,
            history: List<ChatMessage>,
        ) {
            when (val result = ask(question, currentEventId(), history)) {
                is AppResult.Success ->
                    ui.value =
                        ui.value.copy(
                            messages =
                                ui.value.messages + ChatMessage(result.value.reply, false, result.value.uncertain),
                            sending = false,
                        )
                is AppResult.Failure -> fail(result.error, question)
            }
        }

        private suspend fun fail(
            error: AppError,
            question: String,
        ) {
            if (session.endsSession(error)) return
            val problem =
                when (error) {
                    AppError.NotFound -> AssistantProblem.UNAVAILABLE
                    AppError.RateLimited -> AssistantProblem.BUSY
                    else -> AssistantProblem.FAILED
                }
            ui.value = ui.value.copy(sending = false, problem = problem, pending = question)
        }

        private suspend fun currentEventId(): String? {
            if (!eventLoaded) {
                eventId = (events.list() as? AppResult.Success)?.value?.firstOrNull()?.id
                eventLoaded = true
            }
            return eventId
        }
    }
