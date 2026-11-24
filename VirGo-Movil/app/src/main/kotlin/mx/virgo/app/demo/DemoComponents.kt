package mx.virgo.app.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard

@Composable
fun DemoPage(
    title: String,
    subtitle: String = "",
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(DemoMetrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(DemoMetrics.spacing),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Composable
fun DemoCard(
    title: String,
    subtitle: String = "",
    badge: String? = null,
    onClick: (() -> Unit)? = null,
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().heightIn(min = DemoMetrics.minimumCard),
        ) {
            DemoCardCopy(title, subtitle, badge)
        }
    } else {
        VirGoCard(modifier = Modifier.fillMaxWidth()) {
            DemoCardCopy(title, subtitle, badge)
        }
    }
}

@Composable
private fun DemoCardCopy(
    title: String,
    subtitle: String,
    badge: String?,
) {
    Column(
        Modifier.padding(DemoMetrics.spacing),
        verticalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
    ) {
        if (badge != null) {
            Text(badge, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DemoAction(
    label: String,
    onClick: () -> Unit,
) {
    VirGoButton(
        text = label,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = DemoMetrics.touchTarget),
    )
}

@Composable
fun DemoSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge)
}

@Composable
fun DemoStats(items: List<Pair<String, String>>) {
    items.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DemoMetrics.rowSpacing)) {
            pair.forEach { (label, value) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        Modifier.padding(DemoMetrics.spacing),
                        verticalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
                    ) {
                        Text(
                            value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DemoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= DEMO_TEXT_LIMIT) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
fun DemoNotice(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            modifier = Modifier.padding(DemoMetrics.spacing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private const val DEMO_TEXT_LIMIT = 500
