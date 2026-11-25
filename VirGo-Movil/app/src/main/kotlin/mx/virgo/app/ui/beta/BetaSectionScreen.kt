package mx.virgo.app.ui.beta

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.demo.DemoDestinations
import mx.virgo.app.demo.DemoRole
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton

@StringRes
internal fun betaTitle(
    role: DemoRole,
    route: String,
): Int =
    when (route) {
        "payment" -> R.string.beta_payment
        "receipt" -> R.string.beta_receipt
        "booking" -> R.string.beta_booking
        "profile" -> R.string.beta_account
        else -> DemoDestinations.title(role, route)
    }

@Composable
internal fun BetaStatusCard(
    @StringRes title: Int,
    @StringRes description: Int,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun BetaUnavailableScreen(
    @StringRes title: Int,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        BetaStatusCard(R.string.beta_soon, R.string.beta_pending)
        VirGoOutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.demo_back))
        }
    }
}

@Composable
internal fun BetaSectionScreen(
    role: DemoRole,
    route: String,
    accountName: String,
    accountEmail: String,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(betaTitle(role, route)), style = MaterialTheme.typography.headlineMedium)
        if (route == "profile") {
            VirGoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(accountName, style = MaterialTheme.typography.titleLarge)
                    Text(accountEmail)
                    Text(stringResource(role.label), color = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.beta_session_identity),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(stringResource(R.string.beta_logout_hint), style = MaterialTheme.typography.bodyMedium)
        } else {
            BetaStatusCard(R.string.beta_soon, R.string.beta_pending)
            VirGoButton(onClick = {}, enabled = false, text = stringResource(R.string.beta_soon))
        }
    }
}

@Composable
internal fun BetaSectionsMenu(
    role: DemoRole,
    onNavigate: (String) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.beta_sections), style = MaterialTheme.typography.headlineMedium)
        DemoDestinations.forRole(role).forEach { destination ->
            VirGoOutlinedButton(
                onClick = { onNavigate(destination.route) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(betaTitle(role, destination.route)))
            }
        }
    }
}
