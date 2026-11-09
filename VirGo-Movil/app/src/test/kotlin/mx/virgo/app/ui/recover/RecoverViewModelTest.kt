package mx.virgo.app.ui.recover

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import mx.virgo.core.domain.GenerateRecoveryCodeUseCase
import mx.virgo.core.domain.RecoveryError
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoverViewModelTest {
    private val now = 1_700_000_000_000L

    private fun viewModel() =
        RecoverViewModel(
            ValidateEmailUseCase(),
            GenerateRecoveryCodeUseCase(),
            ValidateRecoveryUseCase(),
        )

    @Test
    fun emailValidityUpdatesOnChange() {
        val viewModel = viewModel()
        assertFalse(viewModel.state.value.emailValid)
        viewModel.onEmailChange("ana@mail.com")
        assertTrue(viewModel.state.value.emailValid)
        viewModel.onEmailChange("not-an-email")
        assertFalse(viewModel.state.value.emailValid)
    }

    @Test
    fun sendCodeStoresSixDigitCodeAndEmitsEvent() =
        runTest {
            val viewModel = viewModel()
            viewModel.events.test {
                viewModel.sendCode(now)
                val event = awaitItem() as RecoverEvent.Sent
                assertEquals(6, event.code.length)
                val state = viewModel.state.value
                assertTrue(state.codeSent)
                assertEquals(event.code, state.demoCode)
                assertEquals(now, state.sentAt)
                assertNull(state.stepError)
                expectNoEvents()
            }
        }

    @Test
    fun codeChangeKeepsDigitsOnlyAndMaxSix() {
        val viewModel = viewModel()
        viewModel.sendCode(now)
        viewModel.onCodeChange("12ab3456789")
        assertEquals("123456", viewModel.state.value.code)
    }

    @Test
    fun saveRejectsWrongCode() {
        val viewModel = viewModel()
        viewModel.sendCode(now)
        val generated = viewModel.state.value.demoCode
        val wrong = if (generated == "000000") "000001" else "000000"
        viewModel.onCodeChange(wrong)
        viewModel.onPasswordChange("password1")
        viewModel.onConfirmChange("password1")
        viewModel.save(now)
        assertEquals(RecoveryError.WRONG_CODE, viewModel.state.value.stepError)
    }

    @Test
    fun saveRejectsExpiredCode() {
        val viewModel = viewModel()
        val sentAt = now - 16 * 60 * 1_000
        viewModel.sendCode(sentAt)
        viewModel.onCodeChange(viewModel.state.value.demoCode)
        viewModel.onPasswordChange("password1")
        viewModel.onConfirmChange("password1")
        viewModel.save(now)
        assertEquals(RecoveryError.EXPIRED, viewModel.state.value.stepError)
    }

    @Test
    fun saveRejectsShortPassword() {
        val viewModel = viewModel()
        viewModel.sendCode(now)
        viewModel.onCodeChange(viewModel.state.value.demoCode)
        viewModel.onPasswordChange("pass123")
        viewModel.onConfirmChange("pass123")
        viewModel.save(now)
        assertEquals(RecoveryError.SHORT_PASSWORD, viewModel.state.value.stepError)
    }

    @Test
    fun saveRejectsMismatchedConfirmation() {
        val viewModel = viewModel()
        viewModel.sendCode(now)
        viewModel.onCodeChange(viewModel.state.value.demoCode)
        viewModel.onPasswordChange("password1")
        viewModel.onConfirmChange("password2")
        viewModel.save(now)
        assertEquals(RecoveryError.MISMATCH, viewModel.state.value.stepError)
    }

    @Test
    fun saveWithValidDataClearsErrorAndEmitsDone() =
        runTest {
            val viewModel = viewModel()
            viewModel.events.test {
                viewModel.sendCode(now)
                val sent = awaitItem() as RecoverEvent.Sent
                viewModel.onCodeChange(sent.code)
                viewModel.onPasswordChange("password1")
                viewModel.onConfirmChange("password1")
                viewModel.save(now)
                assertEquals(RecoverEvent.Done, awaitItem())
                assertNull(viewModel.state.value.stepError)
                expectNoEvents()
            }
        }
}
