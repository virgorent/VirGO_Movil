package mx.virgo.app.session

import kotlinx.coroutines.test.runTest
import mx.virgo.core.testing.sampleAuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SessionRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-23T00:00:00Z"), ZoneOffset.UTC)
    private val store = FakeSessionTokenStore()
    private val repository = SessionRepository(store, clock)

    @Test
    fun restoresIdentityWithoutExposingTokensToUi() =
        runTest {
            assertTrue(repository.establish(sampleAuthSession()))
            val restored = SessionRepository(store, clock)
            restored.refresh()
            assertEquals(
                SessionRole.CLIENT,
                restored.state.value.session
                    ?.role,
            )
            assertEquals(
                sampleAuthSession().user.id,
                restored.state.value.session
                    ?.accountId,
            )
            assertFalse(restored.state.value.loading)
            assertFalse(restored.state.value.error)
        }

    @Test
    fun mapsOnlyTheThreeDocumentedRoles() =
        runTest {
            val auth = sampleAuthSession()
            listOf("client" to SessionRole.CLIENT, "owner" to SessionRole.OWNER, "provider" to SessionRole.PROVIDER)
                .forEach { (role, expected) ->
                    assertTrue(repository.establish(auth.copy(user = auth.user.copy(role = role))))
                    assertEquals(
                        expected,
                        repository.state.value.session
                            ?.role,
                    )
                }
        }

    @Test
    fun unknownRoleClearsPreviousSessionInsteadOfFallingBackToClient() =
        runTest {
            assertTrue(repository.establish(sampleAuthSession()))
            val auth = sampleAuthSession()
            assertFalse(repository.establish(auth.copy(user = auth.user.copy(role = "admin"))))
            assertNull(repository.state.value.session)
            assertNull(store.stored.value)
            assertTrue(repository.state.value.error)
        }

    @Test
    fun expiryAtBoundaryClearsSession() =
        runTest {
            repository.establish(sampleAuthSession())
            val expiredClock = Clock.offset(clock, java.time.Duration.ofSeconds(sampleAuthSession().expiresIn))
            val restored = SessionRepository(store, expiredClock)
            restored.refresh()
            assertNull(restored.state.value.session)
            assertNull(store.stored.value)
        }

    @Test
    fun clockRollbackFailsClosed() =
        runTest {
            repository.establish(sampleAuthSession())
            val pastClock = Clock.offset(clock, java.time.Duration.ofMillis(-1))
            val restored = SessionRepository(store, pastClock)
            restored.refresh()
            assertNull(restored.state.value.session)
            assertNull(store.stored.value)
        }

    @Test
    fun failedWriteRollsBackTokenAndMetadataTogether() =
        runTest {
            repository.establish(sampleAuthSession())
            store.failSave = true
            val auth = sampleAuthSession()
            assertFalse(repository.establish(auth.copy(user = auth.user.copy(id = "another-account"))))
            assertNull(repository.state.value.session)
            assertNull(store.stored.value)
            repository.refresh()
            assertNull(repository.state.value.session)
        }

    @Test
    fun logoutClearsDiskAndCannotBeUndoneByRefresh() =
        runTest {
            repository.establish(sampleAuthSession())
            repository.logout()
            repository.refresh()
            assertNull(repository.state.value.session)
            assertNull(store.stored.value)
            val restored = SessionRepository(store, clock)
            restored.refresh()
            assertNull(restored.state.value.session)
        }

    @Test
    fun logoutDiskFailureRemainsDeniedInMemoryAndReportsError() =
        runTest {
            repository.establish(sampleAuthSession())
            store.failClear = true
            repository.logout()
            assertNull(repository.state.value.session)
            assertTrue(repository.state.value.error)
            repository.refresh()
            assertNull(repository.state.value.session)
        }
}
