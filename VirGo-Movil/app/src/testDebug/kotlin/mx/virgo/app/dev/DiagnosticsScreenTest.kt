package mx.virgo.app.dev

import android.content.Context
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.CheckApiHealthUseCase
import mx.virgo.core.testing.FakeHealthRepository
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DiagnosticsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    @Test
    fun healthOkShowsApiAvailable() {
        val viewModel = DiagnosticsViewModel(CheckApiHealthUseCase(FakeHealthRepository()))
        compose.setContent {
            VirGoTheme {
                DiagnosticsScreen(
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        shadowOf(Looper.getMainLooper()).idle()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.diagnostics_api_available)).assertIsDisplayed()
    }

    @Test
    fun apiDownShowsClearErrorWithoutClosing() {
        var backClicked = false
        val fake = FakeHealthRepository(result = AppResult.Failure(AppError.Network))
        val viewModel = DiagnosticsViewModel(CheckApiHealthUseCase(fake))
        compose.setContent {
            VirGoTheme {
                DiagnosticsScreen(
                    onBackClick = { backClicked = true },
                    viewModel = viewModel,
                )
            }
        }
        shadowOf(Looper.getMainLooper()).idle()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.diagnostics_error_network)).assertIsDisplayed()
        assertFalse(backClicked)
    }
}
