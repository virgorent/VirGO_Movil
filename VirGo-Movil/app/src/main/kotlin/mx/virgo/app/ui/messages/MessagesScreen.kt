package mx.virgo.app.ui.messages

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.Conversation
import mx.virgo.core.domain.Message
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MessagesInbox(viewModel: MessagesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.thread != null || state.composing != null) BackHandler(onBack = viewModel::close)
    MessagesContent(
        state,
        MessagesActions(
            onOpen = viewModel::open,
            onCompose = { viewModel.compose() },
            onVenue = viewModel::compose,
            onInput = viewModel::changeInput,
            onSend = viewModel::send,
            onRetry = viewModel::refresh,
        ),
    )
}

@Composable
internal fun MessagesContent(
    state: MessagesUiState,
    actions: MessagesActions,
) {
    VirGoPageBody {
        val thread = state.thread
        val composing = state.composing
        when {
            thread != null -> Thread(thread, state, actions)
            composing != null -> Compose(composing, state, actions)
            else -> Inbox(state, actions)
        }
    }
}

@Composable
private fun Inbox(
    state: MessagesUiState,
    actions: MessagesActions,
) {
    if (!state.isOwner && state.status == InboxStatus.READY) {
        VirGoButton(
            onClick = actions.onCompose,
            text = stringResource(R.string.messages_new),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    when (state.status) {
        InboxStatus.LOADING -> LoadingState(message = stringResource(R.string.messages_loading))
        InboxStatus.UNAVAILABLE ->
            Text(
                stringResource(R.string.messages_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        InboxStatus.FAILED ->
            ErrorBanner(
                stringResource(R.string.messages_error),
                actionLabel = stringResource(R.string.booking_retry),
                onAction = actions.onRetry,
            )
        InboxStatus.READY ->
            if (state.conversations.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.messages_empty),
                    text =
                        stringResource(
                            if (state.isOwner) R.string.messages_empty_owner else R.string.messages_empty_client,
                        ),
                    iconVector = Icons.Outlined.ChatBubbleOutline,
                )
            } else {
                ConversationList(state, actions.onOpen)
            }
    }
}

@Composable
private fun ConversationList(
    state: MessagesUiState,
    onOpen: (Conversation) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            state.conversations.forEachIndexed { index, conversation ->
                if (index > 0) VirGoRowDivider()
                VirGoRow(
                    title = conversation.title(state.isOwner),
                    subtitle =
                        listOfNotNull(
                            conversation.lastMessage,
                            conversation.lastAt?.let(::formatTime),
                        ).joinToString(" · "),
                    icon = Icons.Outlined.ChatBubbleOutline,
                    standalone = false,
                    onClick = { onOpen(conversation) },
                    trailing = {
                        if (conversation.unread > 0) VirGoBadge(conversation.unread.toString())
                    },
                )
            }
        }
    }
}

@Composable
private fun Thread(
    thread: ThreadState,
    state: MessagesUiState,
    actions: MessagesActions,
) {
    Text(
        thread.conversation.title(state.isOwner),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
    when {
        thread.loading -> LoadingState()
        thread.failed -> Text(stringResource(R.string.messages_thread_error), color = MaterialTheme.colorScheme.error)
        else -> thread.messages.forEach { MessageBubble(it) }
    }
    Composer(state, actions)
}

@Composable
private fun Compose(
    composing: NewConversation,
    state: MessagesUiState,
    actions: MessagesActions,
) {
    Text(stringResource(R.string.messages_pick_venue), style = MaterialTheme.typography.titleSmall)
    if (composing.venues.isEmpty()) {
        Text(stringResource(R.string.messages_no_venues), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        composing.venues.forEach { (id, name) ->
            FilterChip(selected = composing.venueId == id, onClick = { actions.onVenue(id) }, label = { Text(name) })
        }
    }
    Composer(state, actions)
}

@Composable
private fun Composer(
    state: MessagesUiState,
    actions: MessagesActions,
) {
    if (state.sendFailed) {
        Text(
            stringResource(R.string.messages_send_failed),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    val label = stringResource(R.string.messages_input)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            VirGoTextField(
                value = state.input,
                onValueChange = actions.onInput,
                placeholder = label,
                singleLine = false,
                modifier = Modifier.semantics { contentDescription = label },
            )
        }
        IconButton(onClick = actions.onSend, enabled = state.canSend) {
            Icon(Icons.AutoMirrored.Outlined.Send, stringResource(R.string.messages_send))
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val colors = MaterialTheme.colorScheme
    val author = stringResource(if (message.mine) R.string.messages_me else R.string.messages_them)
    val time = message.sentAt?.let(::formatTime).orEmpty()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .clip(BubbleShape)
                .background(if (message.mine) colors.primary else colors.surfaceVariant)
                .padding(12.dp)
                .clearAndSetSemantics { contentDescription = "$author ${message.text}. $time" },
        ) {
            Text(message.text, color = if (message.mine) colors.onPrimary else colors.onSurfaceVariant)
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = if (message.mine) colors.onPrimary else colors.onSurfaceVariant,
            )
        }
    }
}

private fun Conversation.title(isOwner: Boolean): String =
    if (isOwner && clientName.isNotBlank()) "$clientName · $venueName" else venueName

private fun formatTime(instant: java.time.Instant): String =
    instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))

private val BubbleShape = RoundedCornerShape(16.dp)
