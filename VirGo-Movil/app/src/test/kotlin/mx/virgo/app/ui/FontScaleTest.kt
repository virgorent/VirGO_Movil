package mx.virgo.app.ui

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
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
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.RegisterUseCase
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
import mx.virgo.core.testing.FakeVenueRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DOUBLE_FONT_SCALE = 2f

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class FontScaleTest {
    @get:Rule
    val compose = createComposeRule()

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    @Test
    fun provisionalHomeKeepsTextsVisibleAtDoubleFontScale() {
        val viewModel = GuestHomeViewModel(GetVenuesUseCase(FakeVenueRepository()))
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = DOUBLE_FONT_SCALE),
            ) {
                VirGoTheme {
                    GuestHomeScreen(
                        onLoginClick = {},
                        onRegisterClick = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        compose.onNodeWithText(string(R.string.guest_brand)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_login)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_section_label)).assertIsDisplayed()
    }

    @Test
    fun loginKeepsTextsAndActionVisibleAtDoubleFontScale() {
        val viewModel =
            LoginViewModel(
                ValidateLoginFormUseCase(ValidateEmailUseCase()),
                LoginUseCase(FakeAuthRepository()),
            )
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = DOUBLE_FONT_SCALE),
            ) {
                VirGoTheme {
                    LoginScreen(
                        onLoginClick = { _, _ -> },
                        viewModel = viewModel,
                    )
                }
            }
        }

        compose.onNodeWithText(string(R.string.login_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.social_google)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.social_apple)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.login_forgot)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.login_submit)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_no_account)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_switch_register)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.login_submit)).performScrollTo().performClick()
    }

    @Test
    fun registerKeepsFormAndFooterVisibleAtDoubleFontScale() {
        val viewModel =
            RegisterViewModel(
                ValidateRegisterFormUseCase(ValidateEmailUseCase()),
                RegisterUseCase(FakeAuthRepository()),
            )
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = DOUBLE_FONT_SCALE),
            ) {
                VirGoTheme {
                    RegisterScreen(
                        venueId = null,
                        onRegisterSuccess = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        compose.onNodeWithText(string(R.string.register_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_benefit_prices)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_role_client)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_submit)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_have_account)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.register_terms)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recoverKeepsLeadAndActionVisibleAtDoubleFontScale() {
        val viewModel =
            RecoverViewModel(
                ValidateEmailUseCase(),
                GenerateRecoveryCodeUseCase(),
                ValidateRecoveryUseCase(),
            )
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = DOUBLE_FONT_SCALE),
            ) {
                VirGoTheme {
                    RecoverScreen(
                        onDone = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        compose.onNodeWithText(string(R.string.recover_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.recover_lead)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.recover_send)).assertIsDisplayed()
    }
}
