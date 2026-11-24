package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.R
import mx.virgo.core.designsystem.theme.LocalSpacing
import mx.virgo.core.designsystem.theme.VirGoTheme

@Preview(name = "Claro", widthDp = 393, heightDp = 852)
@Preview(name = "Oscuro", widthDp = 393, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compacto, letra grande", widthDp = 320, heightDp = 852, fontScale = 2f)
@Preview(
    name = "Oscuro compacto, letra grande",
    widthDp = 320,
    heightDp = 852,
    fontScale = 2f,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun DesignSystemPreview() {
    VirGoTheme {
        Surface {
            DesignSystemSamples()
        }
    }
}

@Composable
private fun DesignSystemSamples() {
    var email by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(
            text = stringResource(R.string.design_preview_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.design_preview_body),
            style = MaterialTheme.typography.bodyMedium,
        )
        VirGoButton(onClick = {}, text = stringResource(R.string.design_preview_primary))
        VirGoButton(onClick = {}, enabled = false, text = stringResource(R.string.design_preview_disabled))
        VirGoOutlinedButton(onClick = {}) {
            Text(stringResource(R.string.design_preview_secondary))
        }
        VirGoTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.design_preview_email),
        )
        VirGoTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.design_preview_email),
            isError = true,
        )
        Text(
            text = stringResource(R.string.design_preview_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        VirGoCard {
            Column(
                modifier = Modifier.padding(spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(R.string.design_preview_card_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                AiBadgeSample()
            }
        }
    }
}

@Composable
private fun AiBadgeSample() {
    VirGoBadge(
        text = stringResource(R.string.design_preview_badge),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(12.dp),
            )
        },
    )
}
