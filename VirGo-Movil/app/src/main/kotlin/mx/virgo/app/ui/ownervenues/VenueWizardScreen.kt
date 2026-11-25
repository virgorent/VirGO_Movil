package mx.virgo.app.ui.ownervenues

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueStep

@Composable
internal fun VenueWizardRoute(
    key: String,
    venue: OwnerVenue?,
    viewModel: VenueWizardViewModel,
    onClose: (VenueWizardOutcome?) -> Unit,
) {
    LaunchedEffect(key) { viewModel.start(key, venue) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.outcome) { state.outcome?.let(onClose) }
    val actions =
        VenueWizardActions(
            onChange = viewModel::update,
            onGoTo = viewModel::goTo,
            onNext = viewModel::next,
            onSaveDraft = viewModel::saveAsDraft,
            onPublish = viewModel::publish,
            onCancel = { onClose(null) },
        )
    BackHandler {
        if (state.step == VenueStep.DETAILS) onClose(null) else viewModel.goTo(previous(state.step))
    }
    if (state.key == key) VenueWizardContent(state, actions)
}

@Composable
internal fun VenueWizardContent(
    state: VenueWizardUiState,
    actions: VenueWizardActions,
) {
    VirGoPageBody {
        StepHeading(state.step)
        state.error?.let { ErrorBanner(message = stringResource(wizardErrorText(it))) }
        when (state.step) {
            VenueStep.DETAILS -> DetailsStep(state.form, state.fieldErrors, actions.onChange)
            VenueStep.CAPACITY_PRICE -> CapacityStep(state.form, state.fieldErrors, actions.onChange)
            VenueStep.REVIEW -> ReviewStep(state.form, actions.onGoTo)
        }
        WizardActions(state, actions)
    }
}

@Composable
private fun StepHeading(step: VenueStep) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(R.string.venue_wizard_step, step.ordinal + 1),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text =
                stringResource(
                    when (step) {
                        VenueStep.DETAILS -> R.string.venue_wizard_step_details
                        VenueStep.CAPACITY_PRICE -> R.string.venue_wizard_step_capacity
                        VenueStep.REVIEW -> R.string.venue_wizard_step_review
                    },
                ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun WizardActions(
    state: VenueWizardUiState,
    actions: VenueWizardActions,
) {
    val busy = state.busy
    if (state.step == VenueStep.REVIEW) {
        VirGoButton(
            onClick = actions.onPublish,
            enabled = !busy,
            text = stringResource(if (busy) R.string.venue_wizard_saving else R.string.venue_wizard_publish),
        )
        VirGoOutlinedButton(onClick = actions.onSaveDraft, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.venue_wizard_save_draft))
        }
    } else {
        VirGoButton(
            onClick = actions.onNext,
            enabled = !busy,
            text = stringResource(if (busy) R.string.venue_wizard_saving else R.string.venue_wizard_next),
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.step != VenueStep.DETAILS) {
            VirGoOutlinedButton(
                onClick = { actions.onGoTo(previous(state.step)) },
                enabled = !busy,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.venue_wizard_back)) }
        }
        VirGoOutlinedButton(onClick = actions.onCancel, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.venue_wizard_cancel))
        }
    }
}

private fun previous(step: VenueStep): VenueStep = VenueStep.entries[maxOf(step.ordinal - 1, 0)]

private fun wizardErrorText(error: WizardError): Int =
    when (error) {
        WizardError.CHECK_FIELDS -> R.string.venue_error_check_fields
        WizardError.NETWORK -> R.string.venue_error_network
        WizardError.FORBIDDEN -> R.string.venue_error_forbidden
        WizardError.NOT_FOUND -> R.string.venue_error_not_found
        WizardError.SERVER -> R.string.venue_error_server
    }
