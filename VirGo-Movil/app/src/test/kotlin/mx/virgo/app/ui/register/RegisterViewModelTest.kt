package mx.virgo.app.ui.register

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import mx.virgo.core.domain.RegisteredEmailStore
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {
    private fun viewModel() = RegisterViewModel(ValidateRegisterFormUseCase(ValidateEmailUseCase()))

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
    fun duplicateEmailSetsFormErrorWithoutEvent() =
        runTest {
            val email = "dup-${System.nanoTime()}@mail.com"
            RegisteredEmailStore.register(email)
            val viewModel = viewModel()
            viewModel.onNameChange("Ana Lopez")
            viewModel.onEmailChange(email)
            viewModel.onPhoneChange("5512345678")
            viewModel.onPasswordChange("password1")
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterFormError.DUPLICATE, viewModel.state.value.formError)
                assertFalse(viewModel.state.value.registered)
                expectNoEvents()
            }
        }

    @Test
    fun validSubmitMarksRegisteredAndEmitsEvent() =
        runTest {
            val viewModel = viewModel()
            viewModel.onNameChange("Ana Lopez")
            viewModel.onEmailChange("ok-${System.nanoTime()}@mail.com")
            viewModel.onPhoneChange("5512345678")
            viewModel.onPasswordChange("password1")
            viewModel.events.test {
                viewModel.submit()
                assertEquals(RegisterEvent.Registered, awaitItem())
                assertTrue(viewModel.state.value.registered)
                assertNull(viewModel.state.value.formError)
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
