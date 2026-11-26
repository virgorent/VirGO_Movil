package mx.virgo.app.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.LogoutUseCase
import mx.virgo.core.testing.FakeAccountRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import org.junit.Assert.assertEquals
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

    private val account = FakeAccountRepository()
    private val store = FakeSessionTokenStore()

    private suspend fun signedIn(): Pair<SessionRepository, SessionViewModel> {
        val repository = SessionRepository(store, clock)
        repository.establish(sampleAuthSession())
        return repository to SessionViewModel(repository, LogoutUseCase(account))
    }

    @Test
    fun confirmedLogoutRevokesRemotelyThenClearsWithoutNotice() =
        runTest {
            val (_, viewModel) = signedIn()
            viewModel.logout()
            runCurrent()
            assertEquals(1, account.logoutCalls)
            assertNull(viewModel.state.value.session)
            assertNull(viewModel.state.value.endReason)
            assertNull(store.stored.value)
        }

    @Test
    fun unauthorizedLogoutMeansAlreadyRevokedAndShowsNoError() =
        runTest {
            account.logoutResult = AppResult.Failure(AppError.Unauthorized)
            val (_, viewModel) = signedIn()
            viewModel.logout()
            runCurrent()
            assertNull(viewModel.state.value.session)
            assertNull(viewModel.state.value.endReason)
        }

    @Test
    fun failedLogoutStillClearsTokenAndReportsUnconfirmed() =
        runTest {
            account.logoutResult = AppResult.Failure(AppError.Network)
            val (_, viewModel) = signedIn()
            viewModel.logout()
            runCurrent()
            assertNull(viewModel.state.value.session)
            assertNull(store.stored.value)
            assertEquals(SessionEndReason.LOGOUT_UNCONFIRMED, viewModel.state.value.endReason)
        }

    @Test
    fun slowLogoutTimesOutAndClearsLocally() =
        runTest {
            account.logoutGate = CompletableDeferred()
            val (_, viewModel) = signedIn()
            viewModel.logout()
            advanceTimeBy(LOGOUT_TIMEOUT_MILLIS - 1)
            runCurrent()
            assertTrue(viewModel.state.value.loggingOut)
            assertTrue(viewModel.state.value.session != null)
            advanceTimeBy(1)
            runCurrent()
            assertNull(viewModel.state.value.session)
            assertNull(store.stored.value)
            assertEquals(SessionEndReason.LOGOUT_UNCONFIRMED, viewModel.state.value.endReason)
        }

    @Test
    fun secondTapWhileLoggingOutIsIgnored() =
        runTest {
            account.logoutGate = CompletableDeferred()
            val (_, viewModel) = signedIn()
            viewModel.logout()
            viewModel.logout()
            runCurrent()
            account.logoutGate?.complete(Unit)
            runCurrent()
            assertEquals(1, account.logoutCalls)
            assertNull(viewModel.state.value.session)
        }

    @Test
    fun expiryShowsSessionExpiredNoticeOnceAndCanBeConsumed() =
        runTest {
            val (_, viewModel) = signedIn()
            advanceTimeBy(STORED_LIFETIME)
            runCurrent()
            assertNull(viewModel.state.value.session)
            assertEquals(SessionEndReason.EXPIRED, viewModel.state.value.endReason)
            viewModel.consumeEndReason()
            assertNull(viewModel.state.value.endReason)
        }

    @Test
    fun invalidationKeepsFirstReasonAndIgnoresLaterOnes() =
        runTest {
            val (repository, viewModel) = signedIn()
            repository.invalidate(SessionEndReason.REVOKED)
            repository.invalidate(SessionEndReason.ROLE_MISMATCH)
            assertEquals(SessionEndReason.REVOKED, viewModel.state.value.endReason)
            assertNull(store.stored.value)
        }

    @Test
    fun newLoginClearsPreviousNotice() =
        runTest {
            val (repository, viewModel) = signedIn()
            repository.invalidate(SessionEndReason.REVOKED)
            repository.establish(sampleAuthSession())
            assertNull(viewModel.state.value.endReason)
            assertFalse(viewModel.state.value.loggingOut)
        }

    @Test
    fun startsUnauthenticatedAndObservesLoginAndLogout() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            val viewModel = SessionViewModel(repository, LogoutUseCase(account))
            assertFalse(viewModel.state.value.loading)
            assertNull(viewModel.state.value.session)
            repository.establish(sampleAuthSession())
            assertTrue(viewModel.state.value.session != null)
            viewModel.logout()
            runCurrent()
            assertNull(viewModel.state.value.session)
        }

    @Test
    fun expiresWithoutNeedingNavigationOrAnotherRequest() =
        runTest {
            val repository = SessionRepository(FakeSessionTokenStore(), clock)
            repository.establish(sampleAuthSession())
            val viewModel = SessionViewModel(repository, LogoutUseCase(account))
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
            val viewModel = SessionViewModel(repository, LogoutUseCase(account))
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
