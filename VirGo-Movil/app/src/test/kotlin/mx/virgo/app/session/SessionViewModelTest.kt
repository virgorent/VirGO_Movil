package mx.virgo.app.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock =
        object : Clock() {
            override fun getZone(): ZoneId = ZoneOffset.UTC

            override fun withZone(zone: ZoneId): Clock = this

            override fun instant(): Instant =
                Instant
                    .parse("2026-11-23T00:00:00Z")
                    .plusMillis(mainDispatcherRule.testDispatcher.scheduler.currentTime)
        }

    @Test
    fun startsUnauthenticatedAndObservesLoginAndLogout() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            val viewModel = SessionViewModel(repository)
            assertFalse(viewModel.state.value.loading)
            assertNull(viewModel.state.value.session)
            repository.establish(sampleAuthSession())
            assertTrue(viewModel.state.value.session != null)
            viewModel.logout()
            assertNull(viewModel.state.value.session)
        }

    @Test
    fun expiresWithoutNeedingNavigationOrAnotherRequest() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            repository.establish(sampleAuthSession())
            val viewModel = SessionViewModel(repository)
            advanceTimeBy(STORED_LIFETIME)
            runCurrent()
            assertNull(viewModel.state.value.session)
            assertFalse(viewModel.state.value.loading)
        }

    @Test
    fun secondLoginForSameAccountRearmsExpiry() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            repository.establish(sampleAuthSession())
            val viewModel = SessionViewModel(repository)
            advanceTimeBy(1)
            repository.establish(sampleAuthSession())
            advanceTimeBy(STORED_LIFETIME - 1)
            runCurrent()
            assertTrue(viewModel.state.value.session != null)
            advanceTimeBy(1)
            runCurrent()
            assertNull(viewModel.state.value.session)
        }
}

private const val STORED_LIFETIME = 900_000L
