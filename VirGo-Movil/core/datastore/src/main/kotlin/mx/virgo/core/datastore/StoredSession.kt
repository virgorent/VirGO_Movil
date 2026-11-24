package mx.virgo.core.datastore

/** Registro privado de datos: nunca se entrega directamente a la UI. */
data class StoredSession(
    val accessToken: String,
    val account: StoredAccount,
    val issuedAtMillis: Long,
    val expiresAtMillis: Long,
) {
    fun isValidAt(nowMillis: Long): Boolean =
        accessToken.isNotBlank() &&
            account.isValid() &&
            hasValidLifetime(nowMillis)

    private fun hasValidLifetime(nowMillis: Long): Boolean =
        issuedAtMillis > 0 &&
            issuedAtMillis <= nowMillis &&
            expiresAtMillis > nowMillis &&
            expiresAtMillis - issuedAtMillis <= MAX_ACCESS_LIFETIME_MILLIS

    override fun toString(): String = "StoredSession(redacted)"

    companion object {
        const val MAX_ACCESS_LIFETIME_MILLIS = 900_000L
    }
}

data class StoredAccount(
    val accountId: String,
    val name: String,
    val email: String,
    val role: String,
) {
    fun isValid(): Boolean =
        accountId.isNotBlank() && name.isNotBlank() && email.isNotBlank() && role in SUPPORTED_ROLES

    override fun toString(): String = "StoredAccount(redacted)"

    companion object {
        private val SUPPORTED_ROLES = setOf("client", "owner", "provider")
    }
}
