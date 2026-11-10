package mx.virgo.app.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900

@Composable
fun DiagnosticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val errorText =
        state.error?.let { error ->
            stringResource(
                when (error) {
                    AppError.Network -> R.string.diagnostics_error_network
                    else -> R.string.diagnostics_error_server
                },
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
    ) {
        DiagnosticsHeader(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))
        DiagnosticsInfoCard(state = state)
        if (errorText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DiagnosticsErrorText(error = errorText)
        }
        Spacer(modifier = Modifier.height(16.dp))
        VirGoButton(
            onClick = viewModel::refresh,
            text = stringResource(R.string.diagnostics_retry),
        )
    }
}

@Composable
private fun DiagnosticsHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.diagnostics_title),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun DiagnosticsInfoCard(
    state: DiagnosticsUiState,
    modifier: Modifier = Modifier,
) {
    val statusText =
        when {
            state.loading -> stringResource(R.string.diagnostics_checking)
            state.apiAvailable -> stringResource(R.string.diagnostics_api_available)
            else -> stringResource(R.string.diagnostics_api_unavailable)
        }
    val latencyText =
        state.latencyMs?.let { stringResource(R.string.diagnostics_latency_value, it) }
            ?: stringResource(R.string.diagnostics_latency_none)

    VirGoCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            DiagnosticsRow(
                label = stringResource(R.string.diagnostics_version_label),
                value = state.versionName,
            )
            DiagnosticsRow(
                label = stringResource(R.string.diagnostics_api_url_label),
                value = state.apiBaseUrl,
            )
            DiagnosticsRow(
                label = stringResource(R.string.diagnostics_status_label),
                value = statusText,
            )
            DiagnosticsRow(
                label = stringResource(R.string.diagnostics_latency_label),
                value = latencyText,
            )
        }
    }
}

@Composable
private fun DiagnosticsRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate900,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun DiagnosticsErrorText(
    error: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = error,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.fillMaxWidth(),
    )
}
