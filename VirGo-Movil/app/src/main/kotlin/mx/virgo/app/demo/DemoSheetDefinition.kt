package mx.virgo.app.demo

import androidx.annotation.StringRes

data class DemoSheetDefinition(
    val id: String,
    val route: String,
    @param:StringRes val title: Int,
    val roles: Set<DemoRole> = setOf(DemoRole.CLIENT),
)
