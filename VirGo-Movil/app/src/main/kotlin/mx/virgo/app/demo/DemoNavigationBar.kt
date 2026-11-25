package mx.virgo.app.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R

@Composable
internal fun DemoBottomNavigation(
    role: DemoRole,
    route: String,
    onNavigate: (String) -> Unit,
    onIndex: () -> Unit,
    systemInsets: Boolean = true,
    indexLabel: Int = R.string.demo_all_screens,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        val padding = if (systemInsets) Modifier.navigationBarsPadding() else Modifier
        Column(Modifier.fillMaxWidth().then(padding)) {
            if (LocalDensity.current.fontScale >= ADAPTIVE_NAV_SCALE) {
                DemoAdaptiveTabs(role, route, onNavigate)
            } else {
                NavigationBar(
                    windowInsets = WindowInsets(0),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    role.tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab,
                            onClick = { onNavigate(tab) },
                            icon = { Icon(demoTabIcon(tab), contentDescription = null) },
                            label = { Text(stringResource(DemoDestinations.title(role, tab))) },
                            colors = prototypeNavColors(),
                        )
                    }
                }
            }
            TextButton(
                onClick = onIndex,
                modifier = Modifier.fillMaxWidth().heightIn(min = DemoMetrics.touchTarget),
            ) {
                Text(stringResource(indexLabel))
            }
        }
    }
}

@Composable
private fun DemoAdaptiveTabs(
    role: DemoRole,
    route: String,
    onNavigate: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState(),
                ).padding(horizontal = DemoMetrics.rowSpacing),
        horizontalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
    ) {
        role.tabs.forEach { tab ->
            FilterChip(
                selected = route == tab,
                onClick = { onNavigate(tab) },
                label = { Text(stringResource(DemoDestinations.title(role, tab))) },
                modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
            )
        }
    }
}

private fun demoTabIcon(route: String): ImageVector = destinationIcon(route)

/** Ítem activo en violeta sobre píldora `violet-soft`; inactivos en gris medio (`.bottom-nav`). */
@Composable
private fun prototypeNavColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

private const val ADAPTIVE_NAV_SCALE = 1.3f
