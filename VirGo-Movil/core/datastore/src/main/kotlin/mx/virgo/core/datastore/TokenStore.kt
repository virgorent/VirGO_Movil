package mx.virgo.core.datastore

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val token: Flow<String?>

    suspend fun save(token: String)

    suspend fun clear()
}
