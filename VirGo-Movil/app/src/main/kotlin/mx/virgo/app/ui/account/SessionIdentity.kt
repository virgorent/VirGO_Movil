package mx.virgo.app.ui.account

import androidx.annotation.StringRes

/** Identidad ya validada de la sesión: se muestra mientras llega o si falla el servidor. */
data class SessionIdentity(
    val name: String,
    val email: String,
    @param:StringRes val roleLabel: Int,
)
