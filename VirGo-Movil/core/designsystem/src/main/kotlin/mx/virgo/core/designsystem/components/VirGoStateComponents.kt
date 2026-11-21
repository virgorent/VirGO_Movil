package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.R
import mx.virgo.core.designsystem.theme.VirGoTheme

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon?.invoke()
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                action()
            }
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier =
                Modifier
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun EmptyStatePreview() {
    VirGoTheme {
        EmptyState(
            title = stringResource(R.string.design_preview_empty_title),
            text = stringResource(R.string.design_preview_empty_text),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            action = {
                VirGoOutlinedButton(onClick = {}) {
                    Text(stringResource(R.string.design_preview_empty_action))
                }
            },
        )
    }
}

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun LoadingStatePreview() {
    VirGoTheme {
        LoadingState(message = stringResource(R.string.design_preview_loading))
    }
}

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ErrorBannerPreview() {
    VirGoTheme {
        ErrorBanner(
            message = stringResource(R.string.design_preview_error_banner),
            actionLabel = stringResource(R.string.design_preview_retry),
            onAction = {},
        )
    }
}
