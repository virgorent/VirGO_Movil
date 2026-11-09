package mx.virgo.app.ui.login

import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    private fun viewModel() = LoginViewModel(ValidateLoginFormUseCase(ValidateEmailUseCase()))

    @Test
    fun submitWithEmptyFieldsMarksSubmittedWithoutLoggingIn() {
        val viewModel = viewModel()
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.submitted)
        assertFalse(state.emailValid)
        assertFalse(state.passwordValid)
        assertFalse(state.loggedIn)
    }

    @Test
    fun submitWithInvalidEmailDoesNotLogIn() {
        val viewModel = viewModel()
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.submitted)
        assertFalse(state.emailValid)
        assertFalse(state.loggedIn)
    }

    @Test
    fun submitWithValidCredentialsLogsInAndKeepsFields() {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.loggedIn)
        assertTrue(state.emailValid)
        assertTrue(state.passwordValid)
        assertEquals("ana@mail.com", state.email)
        assertEquals("secret1", state.password)
    }

    @Test
    fun fieldValidityUpdatesOnChangeAfterSubmit() {
        val viewModel = viewModel()
        viewModel.submit()
        assertFalse(viewModel.state.value.passwordValid)
        viewModel.onPasswordChange("x")
        assertTrue(viewModel.state.value.passwordValid)
    }
}
