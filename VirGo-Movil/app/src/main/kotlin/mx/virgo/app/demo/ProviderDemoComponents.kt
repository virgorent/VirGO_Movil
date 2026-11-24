package mx.virgo.app.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ProviderChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.heightIn(min = PROVIDER_TOUCH_TARGET.dp),
    )
}

internal object ProviderChart {
    @Composable
    fun Bar(
        label: String,
        fraction: Float,
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
        }
    }
}

private const val PROVIDER_TOUCH_TARGET = 48
