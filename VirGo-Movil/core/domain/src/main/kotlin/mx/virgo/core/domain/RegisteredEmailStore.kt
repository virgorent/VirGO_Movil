package mx.virgo.core.domain

/** HU-26: registros recientes en memoria para detectar correo duplicado (demo). */
object RegisteredEmailStore {
    private val emails = mutableSetOf<String>()

    fun register(email: String): Boolean = emails.add(email)
}
