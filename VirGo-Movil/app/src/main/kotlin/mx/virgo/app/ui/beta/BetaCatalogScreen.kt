package mx.virgo.app.ui.beta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.domain.Venue

@Composable
fun BetaCatalogScreen(
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
    viewModel: BetaCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    BetaCatalogContent(state, viewModel::refresh, modifier, onLogin, onRegister)
}

@Composable
internal fun BetaCatalogContent(
    state: BetaCatalogState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            BetaCatalogHeader()
        }
        if (onLogin != null && onRegister != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VirGoButton(onClick = onLogin, text = stringResource(R.string.beta_login))
                    VirGoOutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.beta_register))
                    }
                }
            }
        }
        when {
            state.loading ->
                item {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.beta_loading))
                }
            state.failed ->
                item {
                    BetaStatusCard(R.string.beta_catalog_error, R.string.beta_catalog_retry_hint)
                }
            state.venues.isEmpty() ->
                item {
                    BetaStatusCard(R.string.beta_catalog_empty, R.string.beta_catalog_empty_hint)
                }
            else ->
                items(state.venues) { venue ->
                    BetaVenueCard(venue)
                }
        }
        if (!state.loading) {
            item {
                VirGoOutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                ) {
                    Text(stringResource(if (state.failed) R.string.beta_retry else R.string.beta_refresh))
                }
            }
        }
    }
}

@Composable
private fun BetaCatalogHeader() {
    Column(
        Modifier.padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.beta_label), style = MaterialTheme.typography.labelLarge)
        Text(stringResource(R.string.beta_catalog), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.beta_catalog_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BetaVenueCard(venue: Venue) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(venue.name, style = MaterialTheme.typography.titleLarge)
            venue.zone?.takeIf { it.isNotBlank() }?.let { Text(it) }
            if (venue.capacity > 0) {
                Text(stringResource(R.string.beta_capacity, venue.capacity))
            }
            VirGoButton(onClick = {}, enabled = false, text = stringResource(R.string.beta_soon))
        }
    }
}
