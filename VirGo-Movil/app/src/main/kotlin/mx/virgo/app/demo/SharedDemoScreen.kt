package mx.virgo.app.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@Composable
internal fun SharedDemoScreen(
    role: DemoRole,
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    when (screen) {
        "profile" -> DemoProfile(role, onNavigate, onNotice)
        "messages" -> DemoMessages(onNavigate)
        "chat" -> DemoChat()
        "notifications" -> DemoNotifications(role, onNavigate)
        "help" -> DemoHelp()
        else -> error("Unknown shared demo route: $screen")
    }
}

@Composable
private fun DemoProfile(
    role: DemoRole,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    val workspace = LocalWorkspace.current
    val profile = workspaceRecord(WorkspaceSection.PROFILE)
    val account = workspaceState()?.accountProfile
    val initialName = stringResource(R.string.demo_profile_name)
    var name by rememberSaveable { mutableStateOf(initialName) }
    val saved = stringResource(R.string.demo_local_saved)
    DemoPage(stringResource(R.string.demo_title_profile), stringResource(R.string.demo_shared_subtitle)) {
        WorkspaceSummary("profile")
        DemoCard(
            title = profile?.name ?: account?.name ?: name,
            subtitle = account?.email ?: stringResource(R.string.demo_profile_email),
            badge = stringResource(R.string.demo_profile_role, stringResource(role.label)),
        )
        if (account != null) DemoNotice(stringResource(R.string.workspace_account_profile))
        DemoField(stringResource(R.string.demo_name_label), profile?.name ?: account?.name ?: name) {
            if (workspace == null) {
                name = it
            } else {
                workspace.confirm(WorkspaceSection.PROFILE, (profile ?: WorkspaceRecord()).copy(name = it))
            }
        }
        DemoAction(stringResource(R.string.demo_save_local)) { onNotice(saved) }
        DemoNotice(stringResource(R.string.demo_no_persistence))
        listOf("messages", "notifications", "help").forEach { route ->
            DemoCard(stringResource(DemoDestinations.title(role, route)), onClick = { onNavigate(route) })
        }
    }
}

@Composable
private fun DemoMessages(onNavigate: (String) -> Unit) {
    DemoPage(stringResource(R.string.demo_title_messages), stringResource(R.string.demo_shared_subtitle)) {
        DemoCard(
            title = stringResource(R.string.demo_conversation_title),
            subtitle = stringResource(R.string.demo_conversation_preview),
            badge = stringResource(R.string.demo_conversation_time),
            onClick = { onNavigate("chat") },
        )
        DemoNotice(stringResource(R.string.demo_chat_intro))
    }
}

@Composable
private fun DemoChat() {
    var draft by rememberSaveable { mutableStateOf("") }
    var localMessage by rememberSaveable { mutableStateOf("") }
    DemoPage(stringResource(R.string.demo_conversation_title), stringResource(R.string.demo_chat_intro)) {
        DemoCard(stringResource(R.string.demo_chat_reply), badge = stringResource(R.string.demo_conversation_time))
        if (localMessage.isNotBlank()) {
            DemoCard(localMessage, badge = stringResource(R.string.demo_chat_local))
        }
        DemoField(stringResource(R.string.demo_chat_input), draft) { draft = it }
        DemoAction(stringResource(R.string.demo_chat_send)) {
            if (draft.isNotBlank()) {
                localMessage = draft
                draft = ""
            }
        }
        DemoNotice(stringResource(R.string.demo_no_persistence))
    }
}

@Composable
private fun DemoNotifications(
    role: DemoRole,
    onNavigate: (String) -> Unit,
) {
    val target =
        when (role) {
            DemoRole.CLIENT -> "quotes"
            DemoRole.OWNER -> "ownerRequests"
            DemoRole.PROVIDER -> "providerRequests"
        }
    DemoPage(stringResource(R.string.demo_title_notifications), stringResource(R.string.demo_shared_subtitle)) {
        DemoCard(
            stringResource(R.string.demo_notice_request),
            stringResource(R.string.demo_notice_request_body),
            onClick = { onNavigate(target) },
        )
        DemoCard(stringResource(R.string.demo_notice_reminder), stringResource(R.string.demo_notice_reminder_body))
    }
}

@Composable
private fun DemoHelp() {
    DemoPage(stringResource(R.string.demo_title_help)) {
        listOf(
            R.string.demo_help_question to R.string.demo_help_answer,
            R.string.demo_help_real_question to R.string.demo_help_real_answer,
            R.string.demo_help_data_question to R.string.demo_help_data_answer,
        ).forEach { (question, answer) ->
            Text(stringResource(question), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(answer), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
