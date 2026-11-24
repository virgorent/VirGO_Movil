package mx.virgo.app.demo

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mx.virgo.app.R

@Composable
internal fun ClientDemoStudio(
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var style by rememberSaveable { mutableStateOf(0) }
    val saved = clientText(R.string.demo_client_design_saved)
    DemoPage(clientText(R.string.demo_client_studio), clientText(R.string.demo_client_ai_example)) {
        ClientDemoDisclaimer()
        WorkspaceSummary("studio")
        StudioTabs(tab) { tab = it }
        when (tab) {
            0 -> StudioStyle(style) { style = it }
            1 -> {
                DemoSection(clientText(R.string.demo_client_layout))
                Text(clientText(R.string.demo_client_table_diagram))
                DemoNotice(clientText(R.string.demo_client_table_plan))
            }
            2 ->
                DemoStats(
                    listOf(
                        clientText(R.string.demo_client_waiters) to clientText(R.string.demo_client_four),
                        clientText(R.string.demo_client_security) to clientText(R.string.demo_client_one),
                        clientText(R.string.demo_client_cleaning) to clientText(R.string.demo_client_two),
                    ),
                )
            STUDIO_IDEAS_TAB -> DemoNotice(clientText(R.string.demo_client_decor_ideas))
        }
        DemoNotice(clientText(R.string.demo_client_studio_assumptions))
        DemoAction(clientText(R.string.demo_client_save_design)) { onNotice(saved) }
        ClientDemoLink(R.string.demo_client_event, "event", onNavigate)
    }
}

@Composable
private fun StudioStyle(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val styles = listOf(clientText(R.string.demo_client_style_garden), clientText(R.string.demo_client_style_modern))
    DemoSelector(clientText(R.string.demo_client_change_style), styles, selected, onSelect)
    DemoCard(styles[selected], clientText(R.string.demo_client_style_description))
}

@Composable
private fun StudioTabs(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val tabs =
        listOf(
            R.string.demo_client_style to Icons.Outlined.AutoAwesome,
            R.string.demo_client_tables to Icons.Outlined.TableRestaurant,
            R.string.demo_client_staff to Icons.Outlined.Groups,
            R.string.demo_client_ideas to Icons.Outlined.Lightbulb,
        )
    PrimaryScrollableTabRow(selectedTabIndex = selected, edgePadding = DemoMetrics.toolbarSpacing) {
        tabs.forEachIndexed { index, (label, icon) ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(clientText(label)) },
                icon = { Icon(icon, contentDescription = null) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
            )
        }
    }
}

private const val STUDIO_IDEAS_TAB = 3
