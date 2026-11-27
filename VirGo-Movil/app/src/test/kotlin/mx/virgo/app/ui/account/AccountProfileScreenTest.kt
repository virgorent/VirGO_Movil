package mx.virgo.app.ui.account

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.testing.sampleUserProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class AccountProfileScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val identity = SessionIdentity("Sesión Ana", "sesion@example.invalid", R.string.demo_role_client)

    private fun show(
        state: AccountProfileUiState,
        darkTheme: Boolean = false,
        onRetry: () -> Unit = {},
        deleteActions: DeleteAccountActions = DeleteAccountActions(),
    ) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
                VirGoTheme(darkTheme = darkTheme) {
                    AccountProfileContent(state, identity, onRetry, deleteActions = deleteActions)
                }
            }
        }
    }

    @Test
    fun serverProfileShowsPhoneAndVerification() {
        show(AccountProfileUiState(loading = false, profile = sampleUserProfile()))
        compose.onNodeWithText("Datos de tu cuenta", ignoreCase = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ana Lopez").assertIsDisplayed()
        compose.onNodeWithText("+525512345678").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Correo verificado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Sesión Ana").assertDoesNotExist()
    }

    @Test
    fun loadingShowsSessionIdentityWithoutInventingPhoneOrVerification() {
        show(AccountProfileUiState(loading = true), darkTheme = true)
        compose.onNodeWithText("Sesión Ana").assertIsDisplayed()
        compose.onNodeWithText("sesion@example.invalid").assertIsDisplayed()
        compose.onNodeWithText("Correo verificado").assertDoesNotExist()
        compose.onNodeWithText("Consultando tus datos…").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun outdatedDataOffersRetry() {
        var retried = false
        show(AccountProfileUiState(loading = false, outdated = true), onRetry = { retried = true })
        compose.onNodeWithText("No pudimos actualizar", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Reintentar").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(retried) }
    }

    @Test
    fun profileIsReadOnly() {
        show(AccountProfileUiState(loading = false, profile = sampleUserProfile()))
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText)).assertDoesNotExist()
        compose.onNodeWithText("Guardar", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Editar", substring = true).assertDoesNotExist()
    }

    @Test
    fun unverifiedAccountWithoutPhoneIsStatedHonestly() {
        val profile = sampleUserProfile().copy(phone = null, isVerified = false)
        show(AccountProfileUiState(loading = false, profile = profile))
        compose.onNodeWithText("Sin teléfono registrado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Sin verificar").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clientSeesDeleteActionSeparateFromLogout() {
        var requested = false
        show(
            AccountProfileUiState(loading = false, profile = sampleUserProfile(), canDeleteAccount = true),
            deleteActions = DeleteAccountActions(onRequest = { requested = true }),
        )
        compose.onNodeWithText("Borrar mi cuenta").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(requested) }
    }

    @Test
    fun ownerDoesNotSeeDeleteAction() {
        show(AccountProfileUiState(loading = false, profile = sampleUserProfile(role = "owner")))
        compose.onNodeWithText("Borrar mi cuenta").assertDoesNotExist()
    }

    @Test
    fun confirmationExplainsWhatHappensAndCancelSendsNothing() {
        var cancelled = false
        var confirmed = false
        show(
            AccountProfileUiState(loading = false, canDeleteAccount = true, confirmingDelete = true),
            deleteActions = DeleteAccountActions(onCancel = { cancelled = true }, onConfirm = { confirmed = true }),
        )
        compose.onNodeWithText("¿Borrar tu cuenta?").assertIsDisplayed()
        compose.onNodeWithText("no se puede deshacer", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle {
            assertTrue(cancelled)
            assertFalse(confirmed)
        }
    }

    @Test
    fun deletingDisablesBothButtons() {
        show(AccountProfileUiState(loading = false, canDeleteAccount = true, confirmingDelete = true, deleting = true))
        compose.onNodeWithText("Borrando tu cuenta…").assertIsDisplayed()
        compose.onNodeWithText("Borrar cuenta").assertIsNotEnabled()
        compose.onNodeWithText("Cancelar").assertIsNotEnabled()
    }

    @Test
    fun deleteFailureShowsRetryableError() {
        show(AccountProfileUiState(loading = false, canDeleteAccount = true, deleteFailed = true))
        compose.onNodeWithText("No se pudo borrar tu cuenta", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Borrar mi cuenta").performScrollTo().assertIsEnabled()
    }
}
