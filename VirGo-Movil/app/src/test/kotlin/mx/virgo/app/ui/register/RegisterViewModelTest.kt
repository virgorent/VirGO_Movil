package mx.virgo.app.ui.register

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.RegisterUseCase
import mx.virgo.core.domain.RegisteredEmailStore
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeAuthRepository()

    private fun viewModel() =
        RegisterViewModel(
            ValidateRegisterFormUseCase(ValidateEmailUseCase()),
            RegisterUseCase(fakeRepository),
        )

    private fun fillValidForm(
        viewModel: RegisterViewModel,
        email: String,
    ) {
        viewModel.onNameChange("Ana Lopez")
        viewModel.onEmailChange(email)
        viewModel.onPhoneChange("5512345678")
        viewModel.onPasswordChange("password1")
    }

    @Test
    fun phoneChangeKeepsDigitsOnlyAndMaxTen() {
        val viewModel = viewModel()
        viewModel.onPhoneChange("5a5b123456789012")
        assertEquals("5512345678", viewModel.state.value.phone)
    }

    @Test
    fun submitWithoutValidFormMarksSubmittedAndShowsFieldErrors() {
        val viewModel = viewModel()
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.submitted)
        assertFalse(state.nameValid)
        assertFalse(state.emailValid)
        assertFalse(state.phoneValid)
        assertFalse(state.passwordValid)
        assertNull(state.formError)
        assertFalse(state.registered)
        assertEquals(0, fakeRepository.registerCalls)
    }

    @Test
    fun validityRecomputesAsFieldsChange() {
        val viewModel = viewModel()
        viewModel.submit()
        viewModel.onNameChange("Ana Lopez")
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPhoneChange("5512345678")
        viewModel.onPasswordChange("password1")
        val state = viewModel.state.value
        assertTrue(state.nameValid)
        assertTrue(state.emailValid)
        assertTrue(state.phoneValid)
        assertTrue(state.passwordValid)
    }

    @Test
    fun duplicateEmailSetsFormErrorWithoutCallingApi() =
        runTest {
            val email = "dup-${System.nanoTime()}@mail.com"
            RegisteredEmailStore.register(email)
            val viewModel = viewModel()
            fillValidForm(viewModel, email)
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterFormError.DUPLICATE, viewModel.state.value.formError)
                assertFalse(viewModel.state.value.registered)
                assertEquals(0, fakeRepository.registerCalls)
                expectNoEvents()
            }
        }

    @Test
    fun validSubmitCallsApiAndEmitsEvent() =
        runTest {
            val email = "ok-${System.nanoTime()}@mail.com"
            val viewModel = viewModel()
            fillValidForm(viewModel, email)
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterEvent.Registered, awaitItem())
                assertTrue(viewModel.state.value.registered)
                assertNull(viewModel.state.value.formError)
                assertFalse(viewModel.state.value.isLoading)
                assertEquals(1, fakeRepository.registerCalls)
                assertEquals(mx.virgo.core.domain.AccountRole.CLIENT, fakeRepository.lastRegisterRole)
                expectNoEvents()
            }
        }

    @Test
    fun ownerRoleIsSentToTheServer() =
        runTest {
            val viewModel = viewModel()
            fillValidForm(viewModel, "owner-${System.nanoTime()}@mail.com")
            viewModel.onRoleChange(RegisterRole.OWNER)
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterEvent.Registered, awaitItem())
                assertEquals(mx.virgo.core.domain.AccountRole.OWNER, fakeRepository.lastRegisterRole)
            }
        }

    @Test
    fun apiConflictShowsDuplicateErrorWithoutEvent() =
        runTest {
            fakeRepository.registerResult = AppResult.Failure(AppError.Conflict)
            val email = "conflict-${System.nanoTime()}@mail.com"
            val viewModel = viewModel()
            fillValidForm(viewModel, email)
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterFormError.DUPLICATE, viewModel.state.value.formError)
                assertFalse(viewModel.state.value.registered)
                expectNoEvents()
            }
        }

    @Test
    fun apiNetworkFailureShowsGenericErrorWithoutEvent() =
        runTest {
            fakeRepository.registerResult = AppResult.Failure(AppError.Network)
            val email = "offline-${System.nanoTime()}@mail.com"
            val viewModel = viewModel()
            fillValidForm(viewModel, email)
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterFormError.GENERIC, viewModel.state.value.formError)
                assertFalse(viewModel.state.value.registered)
                assertFalse(viewModel.state.value.isLoading)
                expectNoEvents()
            }
        }

    @Test
    fun roleChangeIsStoredInState() {
        val viewModel = viewModel()
        viewModel.onRoleChange(RegisterRole.PROVIDER)
        assertEquals(RegisterRole.PROVIDER, viewModel.state.value.role)
    }
}
