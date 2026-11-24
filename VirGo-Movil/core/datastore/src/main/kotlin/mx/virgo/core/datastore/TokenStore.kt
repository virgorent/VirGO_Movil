package mx.virgo.core.datastore

import kotlinx.coroutines.flow.Flow
import mx.virgo.core.common.AuthTokenProvider

interface TokenStore : AuthTokenProvider {
    val session: Flow<StoredSession?>

    suspend fun saveSession(session: StoredSession)

    /** Compatibilidad de almacenamiento anterior; un token sin metadatos nunca autoriza solicitudes. */
    suspend fun save(token: String)

    suspend fun clear()
}
