package mx.virgo.app.demo

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mx.virgo.app.R

@Composable
internal fun ClientDemoSocial(
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    when (screen) {
        "reminders" -> ClientDemoReminders()
        "share" -> ClientDemoShare(onNotice)
        "invitation" -> ClientDemoInvitation(onNotice)
        "venueReviews" -> ClientDemoReviews(onNavigate)
        "review" -> ClientDemoReview(onNotice)
        "assistant" -> ClientDemoAssistant()
    }
}

@Composable
private fun ClientDemoReminders() {
    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var savedTitle by rememberSaveable { mutableStateOf("") }
    var savedDate by rememberSaveable { mutableStateOf("") }
    DemoPage(clientText(R.string.demo_client_reminders), clientText(R.string.demo_client_no_push)) {
        ClientDemoDisclaimer()
        DemoSection(clientText(R.string.demo_client_upcoming))
        DemoCard(
            clientText(R.string.demo_client_reminder_payment),
            clientText(R.string.demo_client_reminder_payment_date),
        )
        DemoCard(clientText(R.string.demo_client_reminder_rsvp), clientText(R.string.demo_client_reminder_rsvp_date))
        DemoSection(clientText(R.string.demo_client_new_reminder))
        DemoField(clientText(R.string.demo_client_reason), title, { title = it })
        DemoField(clientText(R.string.demo_client_date), date, { date = it })
        DemoAction(clientText(R.string.demo_client_save_local)) {
            savedTitle = title.trim()
            savedDate = date
        }
        if (savedTitle.isNotBlank()) {
            DemoCard(savedTitle, savedDate)
            DemoAction(clientText(R.string.demo_client_remove_reminder)) { savedTitle = "" }
        }
    }
}

@Composable
private fun ClientDemoShare(onNotice: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var editor by rememberSaveable { mutableStateOf(false) }
    var added by rememberSaveable { mutableStateOf(false) }
    val notice = clientText(R.string.demo_client_share_notice)
    DemoPage(clientText(R.string.demo_client_share), clientText(R.string.demo_client_event_name)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(R.string.demo_client_share_permissions))
        DemoField(clientText(R.string.demo_client_collaborator_name), name, { name = it })
        FilterChip(
            selected = editor,
            onClick = { editor = !editor },
            label = { Text(clientText(R.string.demo_client_edit_permission)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoAction(clientText(R.string.demo_client_simulate_invite)) {
            added = name.isNotBlank()
            onNotice(notice)
        }
        if (added) {
            DemoCard(
                name,
                clientText(if (editor) R.string.demo_client_edit_permission else R.string.demo_client_view_permission),
            )
            DemoAction(clientText(R.string.demo_client_revoke_example)) { added = false }
        }
    }
}

@Composable
private fun ClientDemoInvitation(onNotice: (String) -> Unit) {
    val initial = clientText(R.string.demo_client_invitation_message)
    var message by rememberSaveable { mutableStateOf(initial) }
    var violet by rememberSaveable { mutableStateOf(false) }
    val notice = clientText(R.string.demo_client_invitation_notice)
    DemoPage(clientText(R.string.demo_client_invitation), clientText(R.string.demo_client_preview_only)) {
        ClientDemoDisclaimer()
        val templateLabel = if (violet) R.string.demo_client_template_violet else R.string.demo_client_template_classic
        DemoCard(
            clientText(R.string.demo_client_event_name),
            clientText(R.string.demo_client_event_summary),
            badge = clientText(templateLabel),
        )
        Text(message)
        FilterChip(
            selected = violet,
            onClick = { violet = !violet },
            label = { Text(clientText(R.string.demo_client_template_violet)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoField(clientText(R.string.demo_client_invitation_message_label), message, { message = it })
        DemoAction(clientText(R.string.demo_client_simulate_share)) { onNotice(notice) }
    }
}

@Composable
private fun ClientDemoReviews(onNavigate: (String) -> Unit) {
    var topOnly by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_reviews), clientText(R.string.demo_client_venue_name)) {
        ClientDemoDisclaimer()
        DemoStats(
            listOf(
                clientText(R.string.demo_client_rating) to clientText(R.string.demo_client_reviews_average),
                clientText(R.string.demo_client_reviews) to clientText(R.string.demo_client_two),
            ),
        )
        FilterChip(
            selected = topOnly,
            onClick = { topOnly = !topOnly },
            label = { Text(clientText(R.string.demo_client_five_stars_only)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoCard(clientText(R.string.demo_client_reviewer_one), clientText(R.string.demo_client_review_one))
        DemoNotice(clientText(R.string.demo_client_review_reply))
        if (!topOnly) {
            DemoCard(clientText(R.string.demo_client_reviewer_two), clientText(R.string.demo_client_review_two))
        }
        ClientDemoLink(R.string.demo_client_write_review, "review", onNavigate)
    }
}

@Composable
private fun ClientDemoReview(onNotice: (String) -> Unit) {
    var stars by rememberSaveable { mutableStateOf(0) }
    var opinion by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf(false) }
    val notice = clientText(R.string.demo_client_review_saved)
    DemoPage(clientText(R.string.demo_client_write_review), clientText(R.string.demo_client_venue_name)) {
        ClientDemoDisclaimer()
        DemoAction(clientText(R.string.demo_client_change_rating)) { stars = stars % CLIENT_DEMO_MAX_STARS + 1 }
        Text(stringResourceRating(stars))
        DemoField(clientText(R.string.demo_client_opinion), opinion, { opinion = it })
        DemoNotice(clientText(R.string.demo_client_review_validation))
        if (stars > 0 && opinion.trim().length >= CLIENT_DEMO_MIN_REVIEW_LENGTH) {
            DemoAction(clientText(R.string.demo_client_save_review)) {
                saved = true
                onNotice(notice)
            }
        }
        if (saved) DemoCard(clientText(R.string.demo_client_local_review), opinion)
    }
}

@Composable
private fun stringResourceRating(stars: Int): String =
    androidx.compose.ui.res
        .stringResource(R.string.demo_client_rating_format, stars)

@Composable
private fun ClientDemoAssistant() {
    var question by rememberSaveable { mutableStateOf("") }
    var asked by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_assistant), clientText(R.string.demo_client_ai_example)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(R.string.demo_client_assistant_greeting))
        DemoAction(clientText(R.string.demo_client_assistant_budget)) { topic = false }
        DemoAction(clientText(R.string.demo_client_assistant_staff)) { topic = true }
        val answer =
            if (topic) {
                R.string.demo_client_assistant_staff_answer
            } else {
                R.string.demo_client_assistant_budget_answer
            }
        DemoCard(
            clientText(R.string.demo_client_example_answer),
            clientText(answer),
        )
        DemoField(clientText(R.string.demo_client_question), question, { question = it })
        DemoAction(clientText(R.string.demo_client_show_example)) { asked = question.trim() }
        if (asked.isNotBlank()) {
            DemoCard(asked, clientText(R.string.demo_client_assistant_fallback))
        }
        DemoNotice(clientText(R.string.demo_client_ai_not_invoked))
    }
}

private const val CLIENT_DEMO_MAX_STARS = 5
private const val CLIENT_DEMO_MIN_REVIEW_LENGTH = 5
