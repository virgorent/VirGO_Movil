package mx.virgo.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Escala clara", widthDp = 393, heightDp = 852)
@Preview(
    name = "Escala oscura",
    widthDp = 393,
    heightDp = 852,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
internal fun TypographyScalePreview() {
    VirGoTheme {
        Surface {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScaleSample("displayLarge", MaterialTheme.typography.displayLarge)
                ScaleSample("displayMedium", MaterialTheme.typography.displayMedium)
                ScaleSample("displaySmall", MaterialTheme.typography.displaySmall)
                ScaleSample("headlineLarge", MaterialTheme.typography.headlineLarge)
                ScaleSample("headlineMedium", MaterialTheme.typography.headlineMedium)
                ScaleSample("headlineSmall", MaterialTheme.typography.headlineSmall)
                ScaleSample("titleLarge", MaterialTheme.typography.titleLarge)
                ScaleSample("titleMedium", MaterialTheme.typography.titleMedium)
                ScaleSample("titleSmall", MaterialTheme.typography.titleSmall)
                ScaleSample("bodyLarge", MaterialTheme.typography.bodyLarge)
                ScaleSample("bodyMedium", MaterialTheme.typography.bodyMedium)
                ScaleSample("bodySmall", MaterialTheme.typography.bodySmall)
                ScaleSample("labelLarge", MaterialTheme.typography.labelLarge)
                ScaleSample("labelMedium", MaterialTheme.typography.labelMedium)
                ScaleSample("labelSmall", MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ScaleSample(
    name: String,
    style: TextStyle,
) {
    Text(
        text = name,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
