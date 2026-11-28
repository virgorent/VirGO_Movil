package mx.virgo.core.domain

/** Rol público con el que se crea la cuenta; `wire` es el valor del contrato (backend `005`, PR #44). */
enum class AccountRole(
    val wire: String,
) {
    CLIENT("client"),
    OWNER("owner"),
    PROVIDER("provider"),
}
