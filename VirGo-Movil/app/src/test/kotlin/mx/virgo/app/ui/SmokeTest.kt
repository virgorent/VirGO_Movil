package mx.virgo.app.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.app.ui.guest.GuestHomeScreen
import mx.virgo.app.ui.guest.GuestHomeViewModel
import mx.virgo.app.ui.login.LoginScreen
import mx.virgo.app.ui.login.LoginViewModel
import mx.virgo.app.ui.recover.RecoverScreen
import mx.virgo.app.ui.recover.RecoverViewModel
import mx.virgo.app.ui.register.RegisterScreen
import mx.virgo.app.ui.register.RegisterViewModel
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GenerateRecoveryCodeUseCase
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SmokeTest {
    @get:Rule
    val compose = createComposeRule()

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    @Test
    fun guestHomeShowsBrandAndLoginForwards() {
        var loginClicked = false
        compose.setContent {
            VirGoTheme {
                GuestHomeScreen(
                    onLoginClick = { loginClicked = true },
                    onRegisterClick = {},
                    viewModel = GuestHomeViewModel(),
                )
            }
        }
        compose.onNodeWithText(string(R.string.guest_brand)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_login)).performClick()
        assertTrue(loginClicked)
    }

    @Test
    fun loginSubmitForwardsCredentials() {
        var result: Pair<String, String>? = null
        val vm =
            LoginViewModel(
                ValidateLoginFormUseCase(ValidateEmailUseCase()),
                LoginUseCase(FakeAuthRepository()),
            )
        compose.setContent {
            VirGoTheme {
                LoginScreen(
                    onLoginClick = { email, password -> result = email to password },
                    viewModel = vm,
                )
            }
        }
        compose.onNodeWithText(string(R.string.login_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.field_email)).performScrollTo().performClick()
        compose.onNodeWithText(string(R.string.field_email)).performTextInput("ana@mail.com")
        compose.onNodeWithText(string(R.string.field_password)).performScrollTo().performClick()
        compose.onNodeWithText(string(R.string.field_password)).performTextInput("secret1")
        assertEquals("ana@mail.com", vm.state.value.email)
        assertEquals("secret1", vm.state.value.password)
        compose.onNodeWithText(string(R.string.login_submit)).performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(vm.state.value.loggedIn)
        assertEquals("ana@mail.com" to "secret1", result)
    }

    @Test
    fun registerShowsFormFields() {
        compose.setContent {
            VirGoTheme {
                RegisterScreen(
                    venueId = null,
                    viewModel =
                        RegisterViewModel(ValidateRegisterFormUseCase(ValidateEmailUseCase())),
                )
            }
        }
        compose.onNodeWithText(string(R.string.register_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.field_name)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.field_phone)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recoverFlowSendsCodeAndShowsPasswordStep() {
        compose.setContent {
            VirGoTheme {
                RecoverScreen(
                    viewModel =
                        RecoverViewModel(
                            ValidateEmailUseCase(),
                            GenerateRecoveryCodeUseCase(),
                            ValidateRecoveryUseCase(),
                        ),
                )
            }
        }
        compose.onNodeWithText(string(R.string.recover_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.recover_send)).assertIsNotEnabled()
        compose.onNodeWithText(string(R.string.field_email)).performClick()
        compose.onNodeWithText(string(R.string.field_email)).performTextInput("ana@mail.com")
        compose.onNodeWithText(string(R.string.recover_send)).performClick()
        compose.onNodeWithText(string(R.string.field_code)).performScrollTo().assertIsDisplayed()
    }
}
