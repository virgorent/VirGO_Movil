package mx.virgo.app.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import mx.virgo.core.datastore.StoredSession
import mx.virgo.core.datastore.TokenStore
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class FakeSessionTokenStore : TokenStore {
    val stored = MutableStateFlow<StoredSession?>(null)
    var failSave = false
    var failClear = false
    var saveCalls = 0
    var clearCalls = 0
    override val session: Flow<StoredSession?> = stored
    override val token: Flow<String?> = stored.map { it?.accessToken }

    override suspend fun saveSession(session: StoredSession) {
        saveCalls++
        if (failSave) throw IOException("Simulated storage failure")
        stored.value = session
    }

    override suspend fun save(token: String) {
        error("Legacy token writes are not supported")
    }

    override suspend fun clear() {
        clearCalls++
        if (failClear) throw IOException("Simulated storage failure")
        stored.value = null
    }
}

fun fakeSessionRepository(store: FakeSessionTokenStore = FakeSessionTokenStore()): SessionRepository =
    SessionRepository(store, Clock.fixed(Instant.parse("2026-11-23T00:00:00Z"), ZoneOffset.UTC))
