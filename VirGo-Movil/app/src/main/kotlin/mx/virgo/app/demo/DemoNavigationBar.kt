package mx.virgo.app.demo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@Composable
internal fun DemoBottomNavigation(
    role: DemoRole,
    route: String,
    onNavigate: (String) -> Unit,
    onIndex: () -> Unit,
    systemInsets: Boolean = true,
) {
    Surface(tonalElevation = DemoMetrics.barElevation) {
        val padding = if (systemInsets) Modifier.navigationBarsPadding() else Modifier
        Column(Modifier.fillMaxWidth().then(padding)) {
            if (LocalDensity.current.fontScale >= ADAPTIVE_NAV_SCALE) {
                DemoAdaptiveTabs(role, route, onNavigate)
            } else {
                NavigationBar(windowInsets = WindowInsets(0)) {
                    role.tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab,
                            onClick = { onNavigate(tab) },
                            icon = { Icon(demoTabIcon(tab), contentDescription = null) },
                            label = { Text(stringResource(DemoDestinations.title(role, tab))) },
                        )
                    }
                }
            }
            TextButton(
                onClick = onIndex,
                modifier = Modifier.fillMaxWidth().heightIn(min = DemoMetrics.touchTarget),
            ) {
                Text(stringResource(R.string.demo_all_screens))
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

private fun demoTabIcon(route: String): ImageVector =
    when (route) {
        "explore" -> Icons.Default.Place
        "favorites" -> Icons.Default.FavoriteBorder
        "studio" -> Icons.Default.AutoAwesome
        "event", "ownerCalendar", "providerAgenda" -> Icons.Default.CalendarMonth
        "ownerRequests", "providerRequests" -> Icons.Default.RequestQuote
        "messages" -> Icons.AutoMirrored.Filled.Chat
        "profile" -> Icons.Default.AccountCircle
        else -> Icons.Default.Home
    }

private const val ADAPTIVE_NAV_SCALE = 1.3f
