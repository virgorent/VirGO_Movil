package mx.virgo.app.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton

@Composable
fun AccountProfileScreen(
    identity: SessionIdentity,
    viewModel: AccountProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    AccountProfileContent(state, identity, onRetry = viewModel::retry)
}

/** Perfil de solo lectura (030 RF-01–RF-04, RF-13–RF-14). */
@Composable
internal fun AccountProfileContent(
    state: AccountProfileUiState,
    identity: SessionIdentity,
    onRetry: () -> Unit,
) {
    val unavailable = stringResource(R.string.account_unavailable_value)
    val profile = state.profile
    val noPhone = stringResource(R.string.account_no_phone)
    val phone = if (profile == null) unavailable else profile.phone ?: noPhone
    val verification =
        when {
            profile == null -> unavailable
            profile.isVerified -> stringResource(R.string.account_verified)
            else -> stringResource(R.string.account_not_verified)
        }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.beta_account), style = MaterialTheme.typography.headlineMedium)
        VirGoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val source = if (profile != null) R.string.account_server_data else R.string.beta_session_identity
                Text(
                    stringResource(source),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(profile?.name ?: identity.name, style = MaterialTheme.typography.titleLarge)
                Text(profile?.email ?: identity.email)
                Text(stringResource(identity.roleLabel), color = MaterialTheme.colorScheme.primary)
                LabeledValue(R.string.account_phone, phone)
                LabeledValue(R.string.account_verification, verification)
            }
        }
        ProfileStatus(state, onRetry)
        Text(stringResource(R.string.beta_logout_hint), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProfileStatus(
    state: AccountProfileUiState,
    onRetry: () -> Unit,
) {
    when {
        state.loading ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator()
                Text(stringResource(R.string.account_loading))
            }
        state.outdated ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.account_outdated),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VirGoOutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.account_retry))
                }
            }
    }
}

@Composable
private fun LabeledValue(
    @StringRes label: Int,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
