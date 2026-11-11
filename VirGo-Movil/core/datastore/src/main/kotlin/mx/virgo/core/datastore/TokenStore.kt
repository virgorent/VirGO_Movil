package mx.virgo.core.datastore

import mx.virgo.core.common.AuthTokenProvider

interface TokenStore : AuthTokenProvider {
    suspend fun save(token: String)

    suspend fun clear()
}
