package mx.virgo.app.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.studio.AiLabel
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.ChatMessage

@Composable
fun ClientAssistant(viewModel: AssistantViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    AssistantContent(
        state,
        AssistantActions(onInput = viewModel::changeInput, onSend = viewModel::send, onRetry = viewModel::retry),
    )
}

@Composable
internal fun AssistantContent(
    state: AssistantUiState,
    actions: AssistantActions,
) {
    VirGoPageBody {
        Bubble(ChatMessage(stringResource(R.string.assistant_greeting), fromUser = false))
        if (state.messages.isEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                stringArrayResource(R.array.assistant_suggestions).forEach { question ->
                    AssistChip(onClick = { actions.onSend(question) }, label = { Text(question) })
                }
            }
        }
        state.messages.forEach { message ->
            Bubble(message)
            if (!message.fromUser) AiLabel()
        }
        if (state.sending) {
            Text(stringResource(R.string.assistant_thinking), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.problem?.let { problem ->
            ErrorBanner(
                message = stringResource(problem.text()),
                actionLabel =
                    if (problem ==
                        AssistantProblem.UNAVAILABLE
                    ) {
                        null
                    } else {
                        stringResource(R.string.booking_retry)
                    },
                onAction = if (problem == AssistantProblem.UNAVAILABLE) null else actions.onRetry,
            )
        }
        InputRow(state, actions)
    }
}

@Composable
private fun InputRow(
    state: AssistantUiState,
    actions: AssistantActions,
) {
    val label = stringResource(R.string.assistant_input)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            VirGoTextField(
                value = state.input,
                onValueChange = actions.onInput,
                placeholder = label,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { actions.onSend(state.input) }),
                singleLine = false,
                modifier = Modifier.semantics { contentDescription = label },
            )
        }
        IconButton(onClick = { actions.onSend(state.input) }, enabled = state.canSend) {
            Icon(Icons.AutoMirrored.Outlined.Send, stringResource(R.string.assistant_send))
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage) {
    val author = stringResource(if (message.fromUser) R.string.assistant_you else R.string.assistant_bot)
    val uncertain = if (message.uncertain) stringResource(R.string.assistant_uncertain) else null
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .clip(BubbleShape)
                .background(if (message.fromUser) colors.primary else colors.surfaceVariant)
                .padding(12.dp)
                .clearAndSetSemantics {
                    contentDescription = listOfNotNull("$author ${message.text}", uncertain).joinToString(". ")
                    if (!message.fromUser) liveRegion = LiveRegionMode.Polite
                },
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(message.text, color = if (message.fromUser) colors.onPrimary else colors.onSurfaceVariant)
            uncertain?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
        }
    }
}

private fun AssistantProblem.text(): Int =
    when (this) {
        AssistantProblem.UNAVAILABLE -> R.string.assistant_unavailable
        AssistantProblem.BUSY -> R.string.ai_busy
        AssistantProblem.FAILED -> R.string.assistant_failed
    }

private val BubbleShape = RoundedCornerShape(16.dp)
