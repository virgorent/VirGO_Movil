package mx.virgo.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.R
import mx.virgo.core.designsystem.theme.VirGoTheme

@Composable
fun VirGoBottomBar(
    items: List<VirGoBottomBarItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
            ) {
                items.forEachIndexed { index, item ->
                    BottomBarItem(
                        item = item,
                        selected = item.selected,
                        onClick = { onItemClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: VirGoBottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    Column(
        modifier =
            modifier
                .heightIn(min = 64.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.Tab)
                .padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp),
            )
            if (item.badgeCount > 0) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (item.badgeCount > MAX_BADGE_COUNT) "99+" else item.badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private const val MAX_BADGE_COUNT = 99

@Preview(name = "Claro", widthDp = 393)
@Preview(name = "Oscuro", widthDp = 393, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VirGoBottomBarPreview() {
    VirGoTheme {
        var selected by remember { mutableIntStateOf(0) }
        VirGoBottomBar(
            items =
                listOf(
                    VirGoBottomBarItem(
                        label = stringResource(R.string.design_preview_tab_home),
                        icon = Icons.Filled.Home,
                        selected = selected == 0,
                    ),
                    VirGoBottomBarItem(
                        label = stringResource(R.string.design_preview_tab_explore),
                        icon = Icons.Filled.Place,
                        selected = selected == 1,
                    ),
                    VirGoBottomBarItem(
                        label = stringResource(R.string.design_preview_tab_favorites),
                        icon = Icons.Filled.Favorite,
                        selected = selected == 2,
                        badgeCount = 3,
                    ),
                    VirGoBottomBarItem(
                        label = stringResource(R.string.design_preview_tab_event),
                        icon = Icons.Filled.CalendarMonth,
                        selected = selected == 3,
                    ),
                    VirGoBottomBarItem(
                        label = stringResource(R.string.design_preview_tab_profile),
                        icon = Icons.Filled.Person,
                        selected = selected == 4,
                    ),
                ),
            onItemClick = { selected = it },
        )
    }
}
