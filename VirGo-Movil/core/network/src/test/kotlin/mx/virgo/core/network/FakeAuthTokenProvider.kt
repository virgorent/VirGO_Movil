package mx.virgo.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import mx.virgo.core.common.AuthTokenProvider

internal class FakeAuthTokenProvider(
    private val value: String?,
) : AuthTokenProvider {
    override val token: Flow<String?> = flowOf(value)
}
