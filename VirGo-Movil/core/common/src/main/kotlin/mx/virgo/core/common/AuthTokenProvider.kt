package mx.virgo.core.common

import kotlinx.coroutines.flow.Flow

interface AuthTokenProvider {
    val token: Flow<String?>
}
