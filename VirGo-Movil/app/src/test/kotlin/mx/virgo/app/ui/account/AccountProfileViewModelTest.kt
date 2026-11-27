package mx.virgo.app.ui.account

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.DeleteAccountUseCase
import mx.virgo.core.domain.GetProfileUseCase
import mx.virgo.core.testing.FakeAccountRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AccountProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val account = FakeAccountRepository()
    private val store = FakeSessionTokenStore()
    private val session = SessionRepository(store, Clock.fixed(Instant.parse("2026-11-23T00:00:00Z"), ZoneOffset.UTC))

    private suspend fun viewModel(role: String = "client"): AccountProfileViewModel {
        val auth = sampleAuthSession()
        session.establish(auth.copy(user = auth.user.copy(role = role)))
        return AccountProfileViewModel(GetProfileUseCase(account), DeleteAccountUseCase(account), session)
    }

    @Test
    fun matchingRolePublishesServerProfile() =
        runTest {
            val state = viewModel().state.value
            assertFalse(state.loading)
            assertEquals("+525512345678", state.profile?.phone)
            assertTrue(state.profile?.isVerified == true)
            assertTrue(session.state.value.session != null)
        }

    @Test
    fun differentRoleClosesSessionWithoutPublishingProfile() =
        runTest {
            account.profileResult = AppResult.Success(sampleUserProfile(role = "owner"))
            val state = viewModel().state.value
            assertNull(state.profile)
            assertNull(session.state.value.session)
            assertNull(store.stored.value)
            assertEquals(SessionEndReason.ROLE_MISMATCH, session.state.value.endReason)
        }

    @Test
    fun unknownRoleIsRejected() =
        runTest {
            account.profileResult = AppResult.Success(sampleUserProfile(role = "admin"))
            viewModel()
            assertEquals(SessionEndReason.ROLE_MISMATCH, session.state.value.endReason)
        }

    @Test
    fun unauthorizedRevokesSession() =
        runTest {
            account.profileResult = AppResult.Failure(AppError.Unauthorized)
            viewModel()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }

    @Test
    fun notFoundMeansAccountUnavailable() =
        runTest {
            account.profileResult = AppResult.Failure(AppError.NotFound)
            viewModel()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.ACCOUNT_UNAVAILABLE, session.state.value.endReason)
        }

    @Test
    fun networkFailureKeepsSessionAndMarksDataOutdatedThenRetryRecovers() =
        runTest {
            account.profileResult = AppResult.Failure(AppError.Network)
            val viewModel = viewModel()
            assertTrue(viewModel.state.value.outdated)
            assertNull(viewModel.state.value.profile)
            assertTrue(session.state.value.session != null)

            account.profileResult = AppResult.Success(sampleUserProfile())
            viewModel.retry()
            assertFalse(viewModel.state.value.outdated)
            val recovered = viewModel.state.value.profile
            assertEquals("Ana Lopez", recovered?.name)
            assertEquals(2, account.profileCalls)
        }

    @Test
    fun serverErrorAlsoKeepsSession() =
        runTest {
            account.profileResult = AppResult.Failure(AppError.Server("trace"))
            assertTrue(viewModel().state.value.outdated)
            assertNull(session.state.value.endReason)
        }

    @Test
    fun cancellingTheConfirmationSendsNothing() =
        runTest {
            val viewModel = viewModel()
            assertTrue(viewModel.state.value.canDeleteAccount)
            viewModel.requestDelete()
            assertTrue(viewModel.state.value.confirmingDelete)
            viewModel.cancelDelete()
            assertFalse(viewModel.state.value.confirmingDelete)
            assertEquals(0, account.deleteCalls)
        }

    @Test
    fun deletedAccountClearsTheSessionAndTheToken() =
        runTest {
            val viewModel = viewModel()
            viewModel.requestDelete()
            viewModel.confirmDelete()
            assertEquals(1, account.deleteCalls)
            assertNull(session.state.value.session)
            assertNull(store.stored.value)
            assertEquals(SessionEndReason.ACCOUNT_DELETED, session.state.value.endReason)
        }

    @Test
    fun confirmIsSentOnlyOnceWhileDeleting() =
        runTest {
            account.deleteGate = CompletableDeferred()
            val viewModel = viewModel()
            viewModel.requestDelete()
            viewModel.confirmDelete()
            viewModel.confirmDelete()
            viewModel.cancelDelete()
            assertTrue(viewModel.state.value.deleting)
            assertTrue(viewModel.state.value.confirmingDelete)
            account.deleteGate?.complete(Unit)
            assertEquals(1, account.deleteCalls)
        }

    @Test
    fun unauthorizedDeleteClosesTheSessionAndSaysTheAccountWasNotDeleted() =
        runTest {
            account.deleteResult = AppResult.Failure(AppError.Unauthorized)
            val viewModel = viewModel()
            viewModel.requestDelete()
            viewModel.confirmDelete()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.DELETE_EXPIRED, session.state.value.endReason)
        }

    @Test
    fun networkFailureKeepsTheSessionAndShowsTheError() =
        runTest {
            account.deleteResult = AppResult.Failure(AppError.Network)
            val viewModel = viewModel()
            viewModel.requestDelete()
            viewModel.confirmDelete()
            val state = viewModel.state.value
            assertTrue(state.deleteFailed)
            assertFalse(state.deleting)
            assertFalse(state.confirmingDelete)
            assertTrue(session.state.value.session != null)
            assertNull(session.state.value.endReason)
        }

    @Test
    fun ownersCannotDeleteTheirAccountYet() =
        runTest {
            account.profileResult = AppResult.Success(sampleUserProfile(role = "owner"))
            val viewModel = viewModel(role = "owner")
            assertFalse(viewModel.state.value.canDeleteAccount)
            viewModel.requestDelete()
            assertFalse(viewModel.state.value.confirmingDelete)
        }
}
