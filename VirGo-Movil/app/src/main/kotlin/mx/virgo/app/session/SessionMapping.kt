package mx.virgo.app.session

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import mx.virgo.core.datastore.StoredAccount
import mx.virgo.core.datastore.StoredSession
import mx.virgo.core.domain.AuthSession
import java.time.Instant
import java.util.Base64

/** Valores exactos de VirGO_Backend, specs/003-auth-login/plan.md §6.1. */
internal fun sessionRole(value: String): SessionRole? =
    when (value) {
        "client" -> SessionRole.CLIENT
        "owner" -> SessionRole.OWNER
        "provider" -> SessionRole.PROVIDER
        else -> null
    }

internal fun AuthSession.toStoredSession(nowMillis: Long): StoredSession {
    require(tokenType == "Bearer" && sessionRole(user.role) != null) { "Invalid session contract" }
    require(expiresIn in 1..MAX_EXPIRES_IN_SECONDS) { "Invalid session lifetime" }
    val declaredExpiry = Math.addExact(nowMillis, Math.multiplyExact(expiresIn, MILLIS_PER_SECOND))
    val expiresAt = boundedAccessExpiry(accessToken, declaredExpiry)
    require(Instant.parse(refreshTokenExpiresAt).toEpochMilli() >= expiresAt) { "Invalid session expiry" }
    val account = StoredAccount(user.id, user.name, user.email, user.role)
    return StoredSession(accessToken, account, nowMillis, expiresAt).also {
        require(it.isValidAt(nowMillis)) { "Invalid session data" }
    }
}

/** Un claim sin verificar solo puede acortar la vigencia, nunca conceder identidad ni extenderla. */
private fun boundedAccessExpiry(
    token: String,
    declaredExpiry: Long,
): Long {
    val parts = token.split('.')
    if (parts.size == 1) return declaredExpiry
    require(parts.size == JWT_PART_COUNT) { "Invalid access token" }
    val payload = Base64.getUrlDecoder().decode(parts[1]).toString(Charsets.UTF_8)
    val exp =
        requireNotNull(
            Json
                .parseToJsonElement(payload)
                .jsonObject["exp"]
                ?.jsonPrimitive
                ?.longOrNull,
        )
    return minOf(declaredExpiry, Math.multiplyExact(exp, MILLIS_PER_SECOND))
}

private const val MILLIS_PER_SECOND = 1_000L
private const val MAX_EXPIRES_IN_SECONDS = StoredSession.MAX_ACCESS_LIFETIME_MILLIS / MILLIS_PER_SECOND
private const val JWT_PART_COUNT = 3
