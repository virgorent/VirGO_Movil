package mx.virgo.app.ui.ownerhome

/** Rutas a las que lleva «Inicio» (`023` RF-03). */
data class OwnerHomeActions(
    val onVenues: () -> Unit = {},
    val onRequests: () -> Unit = {},
    val onCalendar: () -> Unit = {},
)
