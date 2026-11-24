package mx.virgo.app.demo

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@Composable
internal fun ClientDemoPlanning(
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    when (screen) {
        "event" -> ClientDemoEvent(onNavigate)
        "studio" -> ClientDemoStudio(onNavigate, onNotice)
        "budget" -> ClientDemoBudget(onNavigate)
        "budgetAI" -> ClientDemoBudgetAi(onNotice)
        "payments" -> ClientDemoPayments()
        "guests" -> ClientDemoGuests()
        "tasks" -> ClientDemoTasks()
        "timeline" -> ClientDemoTimeline(onNavigate)
    }
}

@Composable
private fun ClientDemoEvent(onNavigate: (String) -> Unit) {
    val workspace = LocalWorkspace.current
    val event = workspaceRecord(WorkspaceSection.EVENT)
    val initialName = clientText(R.string.demo_client_event_name)
    var name by rememberSaveable { mutableStateOf(initialName) }
    DemoPage(clientText(R.string.demo_client_event), clientText(R.string.demo_client_event_summary)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("event")
        DemoField(clientText(R.string.demo_client_event_name_label), event?.name ?: name, {
            if (workspace == null) {
                name = it
            } else {
                workspace.confirm(WorkspaceSection.EVENT, (event ?: WorkspaceRecord()).copy(name = it))
            }
        })
        DemoStats(
            listOf(
                clientText(R.string.demo_client_guests) to (
                    event?.let {
                        stringResource(R.string.workspace_guests, it.guests)
                    } ?: clientText(R.string.demo_client_people_100)
                ),
                clientText(R.string.demo_client_budget) to (
                    event?.amount?.let {
                        stringResource(R.string.workspace_amount, it)
                    } ?: clientText(R.string.demo_client_budget_amount)
                ),
            ),
        )
        DemoSection(clientText(R.string.demo_client_team))
        DemoCard(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_rental_amount))
        DemoCard(clientText(R.string.demo_client_provider_name), clientText(R.string.demo_client_provider_detail))
        ClientDemoLink(R.string.demo_client_budget, "budget", onNavigate)
        ClientDemoLink(R.string.demo_client_guests, "guests", onNavigate)
        ClientDemoLink(R.string.demo_client_tasks, "tasks", onNavigate)
        ClientDemoLink(R.string.demo_client_timeline, "timeline", onNavigate)
        ClientDemoLink(R.string.demo_client_reminders, "reminders", onNavigate)
        ClientDemoLink(R.string.demo_client_share, "share", onNavigate)
        ClientDemoLink(R.string.demo_client_invitation, "invitation", onNavigate)
        ClientDemoLink(R.string.demo_client_quotes, "quotes", onNavigate)
        ClientDemoLink(R.string.demo_client_bookings, "bookings", onNavigate)
    }
}

@Composable
private fun ClientDemoBudget(onNavigate: (String) -> Unit) {
    val event = workspaceRecord(WorkspaceSection.EVENT)
    DemoPage(clientText(R.string.demo_client_budget), event?.name ?: clientText(R.string.demo_client_event_name)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("budget")
        DemoStats(
            listOf(
                clientText(R.string.demo_client_planned) to (
                    event?.amount?.let {
                        stringResource(R.string.workspace_amount, it)
                    } ?: clientText(R.string.demo_client_budget_amount)
                ),
                clientText(R.string.demo_client_committed) to clientText(R.string.demo_client_committed_amount),
                clientText(R.string.demo_client_available) to clientText(R.string.demo_client_available_amount),
            ),
        )
        LinearProgressIndicator(progress = { CLIENT_DEMO_BUDGET_PROGRESS })
        DemoNotice(clientText(R.string.demo_client_budget_warning))
        DemoSection(clientText(R.string.demo_client_by_category))
        DemoCard(clientText(R.string.demo_client_venue_category), clientText(R.string.demo_client_budget_venue))
        DemoCard(clientText(R.string.demo_client_catering), clientText(R.string.demo_client_budget_catering))
        DemoCard(clientText(R.string.demo_client_other), clientText(R.string.demo_client_budget_other))
        ClientDemoLink(R.string.demo_client_budget_ai, "budgetAI", onNavigate)
        ClientDemoLink(R.string.demo_client_payments, "payments", onNavigate)
    }
}

@Composable
private fun ClientDemoBudgetAi(onNotice: (String) -> Unit) {
    val venueInitial = clientText(R.string.demo_client_budget_venue_raw)
    val foodInitial = clientText(R.string.demo_client_budget_food_raw)
    var venue by rememberSaveable { mutableStateOf(venueInitial) }
    var food by rememberSaveable { mutableStateOf(foodInitial) }
    var applied by rememberSaveable { mutableStateOf(false) }
    val appliedNotice = clientText(R.string.demo_client_plan_applied)
    DemoPage(clientText(R.string.demo_client_budget_ai), clientText(R.string.demo_client_ai_example)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(R.string.demo_client_budget_ai_description))
        DemoField(clientText(R.string.demo_client_venue_budget_label), venue, { venue = it })
        DemoField(clientText(R.string.demo_client_food_budget_label), food, { food = it })
        DemoCard(clientText(R.string.demo_client_remaining_categories), clientText(R.string.demo_client_budget_split))
        DemoAction(clientText(R.string.demo_client_apply_example)) {
            applied = true
            onNotice(appliedNotice)
        }
        if (applied) DemoNotice(clientText(R.string.demo_client_local_plan_only))
    }
}

@Composable
private fun ClientDemoPayments() {
    var concept by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var savedConcept by rememberSaveable { mutableStateOf("") }
    var savedAmount by rememberSaveable { mutableStateOf("") }
    DemoPage(clientText(R.string.demo_client_payments), clientText(R.string.demo_client_manual_ledger)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("payments")
        DemoCard(clientText(R.string.demo_client_deposit), clientText(R.string.demo_client_ledger_deposit))
        DemoCard(clientText(R.string.demo_client_final_payment), clientText(R.string.demo_client_ledger_pending))
        DemoSection(clientText(R.string.demo_client_record_example))
        DemoField(clientText(R.string.demo_client_concept), concept, { concept = it })
        DemoField(clientText(R.string.demo_client_amount_label), amount, { amount = it })
        DemoAction(clientText(R.string.demo_client_save_local)) {
            savedConcept = concept.trim()
            savedAmount = amount.trim()
        }
        if (savedConcept.isNotBlank()) DemoCard(savedConcept, savedAmount)
        DemoNotice(clientText(R.string.demo_client_ledger_note))
    }
}

@Composable
private fun ClientDemoGuests() {
    var query by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var addedName by rememberSaveable { mutableStateOf("") }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    val sampleName = clientText(R.string.demo_client_guest_name)
    DemoPage(clientText(R.string.demo_client_guests), clientText(R.string.demo_client_synthetic_contacts)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("guests")
        DemoField(clientText(R.string.demo_client_find_guest), query, { query = it })
        if (sampleName.contains(query, ignoreCase = true)) {
            DemoCard(
                sampleName,
                clientText(R.string.demo_client_guest_table),
                badge = clientText(if (confirmed) R.string.demo_client_confirmed else R.string.demo_client_pending),
            )
            DemoAction(clientText(R.string.demo_client_toggle_rsvp)) { confirmed = !confirmed }
        }
        DemoSection(clientText(R.string.demo_client_add_guest))
        DemoField(clientText(R.string.demo_client_name_label), name, { name = it })
        DemoAction(clientText(R.string.demo_client_add_local)) { addedName = name.trim() }
        if (addedName.isNotBlank() && addedName.contains(query, ignoreCase = true)) {
            DemoCard(addedName, clientText(R.string.demo_client_pending))
        }
    }
}

@Composable
private fun ClientDemoTasks() {
    var venue by rememberSaveable { mutableStateOf(true) }
    var invitations by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var added by rememberSaveable { mutableStateOf("") }
    var extraDone by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_tasks), clientText(R.string.demo_client_task_intro)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("tasks")
        Text(clientText(R.string.demo_client_task_venue))
        Checkbox(
            checked = venue,
            onCheckedChange = { venue = it },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        Text(clientText(R.string.demo_client_task_invitations))
        Checkbox(
            checked = invitations,
            onCheckedChange = { invitations = it },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoField(clientText(R.string.demo_client_new_task), title, { title = it })
        DemoAction(clientText(R.string.demo_client_add_local)) { added = title.trim() }
        if (added.isNotBlank()) {
            Text(added)
            Checkbox(
                checked = extraDone,
                onCheckedChange = { extraDone = it },
                modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
            )
        }
    }
}

@Composable
private fun ClientDemoTimeline(onNavigate: (String) -> Unit) {
    DemoPage(clientText(R.string.demo_client_timeline), clientText(R.string.demo_client_event_name)) {
        ClientDemoDisclaimer()
        DemoCard(clientText(R.string.demo_client_stage_1), clientText(R.string.demo_client_stage_1_detail))
        DemoCard(clientText(R.string.demo_client_stage_2), clientText(R.string.demo_client_stage_2_detail))
        DemoCard(clientText(R.string.demo_client_stage_3), clientText(R.string.demo_client_stage_3_detail))
        DemoCard(clientText(R.string.demo_client_stage_4), clientText(R.string.demo_client_stage_4_detail))
        LinearProgressIndicator(progress = { CLIENT_DEMO_TIMELINE_PROGRESS })
        ClientDemoLink(R.string.demo_client_tasks, "tasks", onNavigate)
        ClientDemoLink(R.string.demo_client_reminders, "reminders", onNavigate)
    }
}

private const val CLIENT_DEMO_BUDGET_PROGRESS = 0.85f
private const val CLIENT_DEMO_TIMELINE_PROGRESS = 0.25f
