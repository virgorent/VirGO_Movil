package mx.virgo.core.designsystem.components

import androidx.compose.ui.graphics.vector.ImageVector

data class VirGoBottomBarItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false,
    val badgeCount: Int = 0,
)
