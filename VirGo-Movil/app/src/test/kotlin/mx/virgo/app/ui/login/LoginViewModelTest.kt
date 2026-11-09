package mx.virgo.app.ui.login

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeAuthRepository()

    private fun viewModel() =
        LoginViewModel(
            ValidateLoginFormUseCase(ValidateEmailUseCase()),
            LoginUseCase(fakeRepository),
        )

    @Test
    fun submitWithEmptyFieldsMarksSubmittedWithoutCallingApi() {
        val viewModel = viewModel()
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.submitted)
        assertFalse(state.emailValid)
        assertFalse(state.passwordValid)
        assertFalse(state.loggedIn)
        assertEquals(0, fakeRepository.calls)
    }

    @Test
    fun submitWithInvalidEmailDoesNotCallApi() {
        val viewModel = viewModel()
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.submitted)
        assertFalse(state.emailValid)
        assertFalse(state.loggedIn)
        assertEquals(0, fakeRepository.calls)
    }

    @Test
    fun submitWithValidCredentialsLogsInAndKeepsFields() {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        val state = viewModel.state.value
        assertTrue(state.loggedIn)
        assertNull(state.error)
        assertFalse(state.isLoading)
        assertEquals(1, fakeRepository.calls)
        assertEquals("ana@mail.com", state.email)
        assertEquals("secret1", state.password)
    }

    @Test
    fun failedLoginWithUnauthorizedShowsCredentialError() {
        fakeRepository.result = AppResult.Failure(AppError.Unauthorized)
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPasswordChange("wrong-pass")
        viewModel.submit()
        val state = viewModel.state.value
        assertEquals(AppError.Unauthorized, state.error)
        assertFalse(state.loggedIn)
        assertEquals(1, fakeRepository.calls)
    }

    @Test
    fun failedLoginWithNetworkErrorShowsNetworkError() {
        fakeRepository.result = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        assertEquals(AppError.Network, viewModel.state.value.error)
    }

    @Test
    fun editingFieldsClearsLoginError() {
        fakeRepository.result = AppResult.Failure(AppError.Unauthorized)
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@mail.com")
        viewModel.onPasswordChange("secret1")
        viewModel.submit()
        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
        viewModel.onEmailChange("otro@mail.com")
        assertNull(viewModel.state.value.error)
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
