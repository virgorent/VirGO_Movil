package mx.virgo.app.ui.beta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoIconBubble
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.theme.pageBackground
import mx.virgo.core.domain.Venue

@Composable
fun BetaCatalogScreen(
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
    onOpenVenue: ((String) -> Unit)? = null,
    viewModel: BetaCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    BetaCatalogContent(state, viewModel::refresh, modifier, onLogin, onRegister, onOpenVenue)
}

@Composable
internal fun BetaCatalogContent(
    state: BetaCatalogState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
    onOpenVenue: ((String) -> Unit)? = null,
) {
    Column(modifier.fillMaxSize().background(pageBackground())) {
        VirGoPageHeader(title = stringResource(R.string.beta_catalog), kicker = stringResource(R.string.beta_label))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(stringResource(R.string.beta_catalog_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onLogin != null && onRegister != null) {
                item { AccountActions(onLogin, onRegister) }
            }
            catalogBody(state, onRefresh, onOpenVenue)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.catalogBody(
    state: BetaCatalogState,
    onRefresh: () -> Unit,
    onOpenVenue: ((String) -> Unit)?,
) {
    when {
        state.loading -> item { CatalogLoading() }
        state.failed ->
            item {
                CatalogState(Icons.Outlined.CloudOff, R.string.beta_catalog_error, R.string.beta_catalog_retry_hint) {
                    VirGoButton(onClick = onRefresh, text = stringResource(R.string.beta_retry))
                }
            }
        state.venues.isEmpty() ->
            item {
                CatalogState(Icons.Outlined.Storefront, R.string.beta_catalog_empty, R.string.beta_catalog_empty_hint) {
                    VirGoOutlinedButton(onClick = onRefresh) { Text(stringResource(R.string.beta_refresh)) }
                }
            }
        else -> {
            items(state.venues) { venue -> BetaVenueCard(venue, onOpenVenue) }
            item {
                VirGoOutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.beta_refresh))
                }
            }
        }
    }
}

@Composable
private fun AccountActions(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VirGoButton(onClick = onLogin, text = stringResource(R.string.beta_login))
        VirGoOutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.beta_register))
        }
    }
}

@Composable
private fun CatalogLoading() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.beta_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CatalogState(
    icon: ImageVector,
    title: Int,
    text: Int,
    action: @Composable () -> Unit,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        EmptyState(
            title = stringResource(title),
            text = stringResource(text),
            iconVector = icon,
            action = action,
        )
    }
}

@Composable
private fun BetaVenueCard(
    venue: Venue,
    onOpen: ((String) -> Unit)?,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VirGoIconBubble(Icons.Outlined.Storefront, size = VENUE_ICON_DP)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(venue.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    venue.zone?.takeIf { it.isNotBlank() }?.let { VenueFact(Icons.Outlined.Place, it) }
                }
            }
            if (venue.capacity > 0) {
                VenueFact(Icons.Outlined.Groups, stringResource(R.string.beta_capacity, venue.capacity))
            }
            // 006 RF-01: «Ver detalle» cuando hay a dónde ir; sin destino sigue «Próximamente».
            if (onOpen != null) {
                VirGoButton(onClick = { onOpen(venue.id) }, text = stringResource(R.string.venue_detail_open))
            } else {
                VirGoButton(onClick = {}, enabled = false, text = stringResource(R.string.beta_soon))
            }
        }
    }
}

@Composable
private fun VenueFact(
    icon: ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private const val VENUE_ICON_DP = 44
