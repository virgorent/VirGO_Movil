package mx.virgo.app.ui.beta

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.demo.DemoDestinations
import mx.virgo.app.demo.DemoRole
import mx.virgo.app.demo.destinationIcon
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.designsystem.theme.pageBackground

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
internal fun BetaUnavailableScreen(
    @StringRes title: Int,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(pageBackground())) {
        VirGoPageHeader(
            title = stringResource(title),
            onBack = onBack,
            backContentDescription = stringResource(R.string.demo_back),
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VirGoCard(Modifier.fillMaxWidth()) {
                EmptyState(
                    title = stringResource(R.string.beta_soon),
                    text = stringResource(R.string.beta_pending),
                    iconVector = Icons.Outlined.LockReset,
                    action = {
                        VirGoOutlinedButton(onClick = onBack) { Text(stringResource(R.string.demo_back)) }
                    },
                )
            }
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
    if (route == "profile") {
        LegacyAccountSummary(role, accountName, accountEmail)
        return
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(pageBackground())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        VirGoCard(Modifier.fillMaxWidth()) {
            EmptyState(
                title = stringResource(betaTitle(role, route)),
                text = stringResource(R.string.beta_pending),
                iconVector = destinationIcon(route),
                action = { VirGoBadge(text = stringResource(R.string.beta_soon), tone = VirGoTone.GRAY) },
            )
        }
    }
}

/** Resumen de identidad usado fuera del espacio con Hilt (pruebas de `034`); Cuenta real en `030`. */
@Composable
private fun LegacyAccountSummary(
    role: DemoRole,
    accountName: String,
    accountEmail: String,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(betaTitle(role, "profile")), style = MaterialTheme.typography.headlineMedium)
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
    }
}

/** Destinos con servicio conectado; el resto muestra «Próximamente». */
private val connectedRoutes = setOf("profile", "explore")

@Composable
internal fun BetaSectionsMenu(
    role: DemoRole,
    onNavigate: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(pageBackground())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        VirGoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                val destinations = DemoDestinations.forRole(role)
                destinations.forEachIndexed { index, destination ->
                    val available = destination.route in connectedRoutes
                    VirGoRow(
                        title = stringResource(betaTitle(role, destination.route)),
                        icon = destinationIcon(destination.route),
                        tone = if (available) VirGoTone.VIOLET else VirGoTone.GRAY,
                        standalone = false,
                        onClick = { onNavigate(destination.route) },
                        trailing =
                            if (available) {
                                null
                            } else {
                                { VirGoBadge(text = stringResource(R.string.beta_soon), tone = VirGoTone.GRAY) }
                            },
                    )
                    if (index < destinations.lastIndex) VirGoRowDivider()
                }
            }
        }
    }
}
