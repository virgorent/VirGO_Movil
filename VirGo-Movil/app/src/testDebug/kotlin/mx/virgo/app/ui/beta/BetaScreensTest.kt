package mx.virgo.app.ui.beta

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import mx.virgo.app.demo.DemoDestinations
import mx.virgo.app.demo.DemoRole
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Venue
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
class BetaScreensTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyCatalogOffersAccountActionsWithoutSamples() {
        var registered = false
        compose.setContent {
            VirGoTheme {
                BetaCatalogContent(
                    BetaCatalogState(loading = false),
                    onRefresh = {},
                    onLogin = {},
                    onRegister = { registered = true },
                )
            }
        }
        compose.onNodeWithText("Aún no hay salones publicados").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Casa Oliva").assertDoesNotExist()
        compose.onNodeWithText("Recorrer pantallas de muestra", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(registered) }
    }

    @Test
    fun pendingSectionIsDisabledInDarkThemeWithLargeText() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                VirGoTheme(darkTheme = true) {
                    BetaSectionScreen(DemoRole.CLIENT, "studio", "Session name", "session@example.invalid")
                }
            }
        }
        compose.onNodeWithText("IA Studio").assertIsDisplayed()
        compose.onNodeWithText("Simular guardar diseño").assertDoesNotExist()
        // 035 RF-12: insignia «Próximamente» y ninguna acción habilitada.
        compose.onNodeWithText("Próximamente").performScrollTo().assertIsDisplayed()
        compose.onNode(hasClickAction() and isEnabled()).assertDoesNotExist()
    }

    @Test
    fun accountUsesSessionIdentityWithoutLocalEditing() {
        compose.setContent {
            VirGoTheme {
                BetaSectionScreen(DemoRole.PROVIDER, "profile", "Session name", "session@example.invalid")
            }
        }
        compose.onNodeWithText("Session name").assertIsDisplayed()
        compose.onNodeWithText("session@example.invalid").assertIsDisplayed()
        compose.onNodeWithText("Guardar", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Editar perfil").assertDoesNotExist()
    }

    @Test
    fun catalogErrorAllowsRetryWithoutPretendingToBeEmpty() {
        var retried = false
        compose.setContent {
            VirGoTheme {
                BetaCatalogContent(
                    BetaCatalogState(loading = false, failed = true),
                    onRefresh = { retried = true },
                )
            }
        }
        compose.onNodeWithText("No pudimos cargar los salones").assertIsDisplayed()
        compose.onNodeWithText("Aún no hay salones publicados").assertDoesNotExist()
        compose.onNodeWithText("Volver a intentar").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(retried) }
    }

    @Test
    fun catalogLoadingDoesNotClaimItIsEmpty() {
        compose.setContent {
            VirGoTheme { BetaCatalogContent(BetaCatalogState(), onRefresh = {}) }
        }
        compose.onNodeWithText("Cargando salones…").assertIsDisplayed()
        compose.onNodeWithText("Aún no hay salones publicados").assertDoesNotExist()
        compose.onNodeWithText("Volver a intentar").assertDoesNotExist()
    }

    @Test
    fun realCatalogDoesNotInventMissingCapacityOrOfferTransactions() {
        val venue = Venue("real-id", "Publicado por API", null, "Centro", 0, 0.0, 0, null, null)
        compose.setContent {
            VirGoTheme {
                BetaCatalogContent(BetaCatalogState(loading = false, venues = listOf(venue)), onRefresh = {})
            }
        }
        compose.onNodeWithText("Publicado por API").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hasta 0 personas").assertDoesNotExist()
        compose.onNode(hasText("Próximamente") and hasClickAction()).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Reservar").assertDoesNotExist()
    }

    @Test
    fun everyPendingDestinationAcrossAllRolesHasOnlyADisabledAction() {
        val role = mutableStateOf(DemoRole.CLIENT)
        val route = mutableStateOf("studio")
        compose.setContent {
            VirGoTheme {
                BetaSectionScreen(role.value, route.value, "Session name", "session@example.invalid")
            }
        }
        DemoRole.entries.forEach { currentRole ->
            DemoDestinations.forRole(currentRole).filter { it.route !in listOf("profile", "explore") }.forEach {
                compose.runOnIdle {
                    role.value = currentRole
                    route.value = it.route
                }
                compose.onNodeWithText("Próximamente").performScrollTo().assertIsDisplayed()
                compose.onNode(hasClickAction() and isEnabled()).assertDoesNotExist()
                compose.onNodeWithText("Guardar", substring = true).assertDoesNotExist()
                compose.onNodeWithText("Simular", substring = true).assertDoesNotExist()
            }
        }
    }
}
