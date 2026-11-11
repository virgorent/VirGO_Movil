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
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
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
        val viewModel = GuestHomeViewModel()
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
        compose.onNodeWithText(string(R.string.login_submit)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.login_submit)).performClick()
    }
}
