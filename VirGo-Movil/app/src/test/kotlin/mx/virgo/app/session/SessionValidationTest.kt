package mx.virgo.app.session

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import mx.virgo.core.datastore.StoredSession
import mx.virgo.core.datastore.TokenStore
import mx.virgo.core.testing.sampleAuthSession
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SessionValidationTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-23T00:00:00Z"), ZoneOffset.UTC)

    @Test
    fun invalidDurationsNeverPersist() =
        runTest {
            val store = FakeSessionTokenStore()
            val repository = SessionRepository(store, clock)
            listOf(0L, -1L, EXCESSIVE_DURATION_SECONDS, Long.MAX_VALUE).forEach { duration ->
                assertFalse(repository.establish(sampleAuthSession().copy(expiresIn = duration)))
                assertNull(store.stored.value)
                assertNull(repository.state.value.session)
            }
        }

    @Test
    fun invalidRefreshExpiryNeverPersistsEvenThoughRefreshIsNotStored() =
        runTest {
            val store = FakeSessionTokenStore()
            val repository = SessionRepository(store, clock)
            listOf("", "not-a-date", "2026-11-22T00:00:00Z", "2026-11-23T00:00:01Z").forEach { date ->
                assertFalse(repository.establish(sampleAuthSession().copy(refreshTokenExpiresAt = date)))
                assertNull(store.stored.value)
            }
        }

    @Test
    fun invalidTokenTypeOrBlankTokenNeverAuthorizes() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            assertFalse(repository.establish(sampleAuthSession().copy(tokenType = "Basic")))
            assertFalse(repository.establish(sampleAuthSession().copy(accessToken = "")))
            assertNull(repository.state.value.session)
        }

    @Test
    fun corruptStorageFailsClosedWithoutLeakingTheException() =
        runTest {
            val corruptStore =
                object : TokenStore {
                    override val session = flow<StoredSession?> { throw IOException("Corrupt storage") }
                    override val token = flow<String?> { emit(null) }

                    override suspend fun saveSession(session: StoredSession) = error("Read-only test")

                    override suspend fun save(token: String) = error("Read-only test")

                    override suspend fun clear() = Unit
                }
            val repository = SessionRepository(corruptStore, clock)
            repository.refresh()
            assertNull(repository.state.value.session)
            assertFalse(repository.state.value.loading)
            assertTrue(repository.state.value.error)
        }

    @Test
    fun restoredUnknownRoleIsDenied() =
        runTest {
            val store = FakeSessionTokenStore()
            val valid = sampleAuthSession().toStoredSession(clock.millis())
            store.stored.value = valid.copy(account = valid.account.copy(role = "unknown"))
            val repository = SessionRepository(store, clock)
            repository.refresh()
            assertNull(repository.state.value.session)
            assertNull(store.stored.value)
        }
}

private const val EXCESSIVE_DURATION_SECONDS = 901L
