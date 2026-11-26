package mx.virgo.app.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone

@Composable
fun AccountProfileScreen(
    identity: SessionIdentity,
    onLogout: () -> Unit,
    viewModel: AccountProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    AccountProfileContent(state, identity, onRetry = viewModel::retry, onLogout = onLogout)
}

/** Perfil de solo lectura (030 RF-01–RF-04) con el lenguaje del prototipo (035 RF-13). */
@Composable
internal fun AccountProfileContent(
    state: AccountProfileUiState,
    identity: SessionIdentity,
    onRetry: () -> Unit,
    onLogout: () -> Unit = {},
) {
    val profile = state.profile
    val unavailable = stringResource(R.string.account_unavailable_value)
    val noPhone = stringResource(R.string.account_no_phone)
    val phone = if (profile == null) unavailable else profile.phone ?: noPhone
    val verification =
        when {
            profile == null -> unavailable
            profile.isVerified -> stringResource(R.string.account_verified)
            else -> stringResource(R.string.account_not_verified)
        }
    val verificationTone =
        when {
            profile == null -> VirGoTone.GRAY
            profile.isVerified -> VirGoTone.GREEN
            else -> VirGoTone.AMBER
        }
    VirGoPageBody {
        IdentityCard(profile?.name ?: identity.name, profile?.email ?: identity.email, identity.roleLabel)
        val source = if (profile != null) R.string.account_server_data else R.string.beta_session_identity
        VirGoSection(kicker = stringResource(source)) {
            VirGoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    VirGoRow(
                        title = stringResource(R.string.account_phone),
                        subtitle = phone,
                        icon = Icons.Outlined.Phone,
                        standalone = false,
                    )
                    VirGoRowDivider()
                    VirGoRow(
                        title = stringResource(R.string.account_verification),
                        subtitle = verification,
                        icon = Icons.Outlined.Verified,
                        tone = verificationTone,
                        standalone = false,
                    )
                    VirGoRowDivider()
                    VirGoRow(
                        title = stringResource(R.string.account_role),
                        subtitle = stringResource(identity.roleLabel),
                        icon = Icons.Outlined.Badge,
                        standalone = false,
                    )
                }
            }
        }
        ProfileStatus(state, onRetry)
        Text(
            stringResource(R.string.beta_logout_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LogoutButton(onLogout)
    }
}

@Composable
private fun IdentityCard(
    name: String,
    email: String,
    @StringRes roleLabel: Int,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(AVATAR_DP.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials(name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VirGoBadge(text = stringResource(roleLabel), tone = VirGoTone.VIOLET)
            }
        }
    }
}

@Composable
private fun ProfileStatus(
    state: AccountProfileUiState,
    onRetry: () -> Unit,
) {
    when {
        state.loading ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Text(stringResource(R.string.account_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        state.outdated ->
            VirGoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) {
        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.beta_local_logout), Modifier.padding(start = 8.dp))
    }
}

internal fun initials(name: String): String =
    name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

private const val AVATAR_DP = 56
