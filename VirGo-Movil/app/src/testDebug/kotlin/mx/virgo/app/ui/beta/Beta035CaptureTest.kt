package mx.virgo.app.ui.beta

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import mx.virgo.app.R
import mx.virgo.app.demo.DemoRole
import mx.virgo.app.demo.RoleWorkspace
import mx.virgo.app.ui.account.AccountProfileContent
import mx.virgo.app.ui.account.AccountProfileUiState
import mx.virgo.app.ui.account.SessionIdentity
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Venue
import mx.virgo.core.testing.sampleUserProfile
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Evidencia visual de `035` (T3501/T3509). Solo genera PNG si existe el archivo
 * `build/capture-035/enabled`; en el pipeline normal se omite. Datos sintéticos
 * identificables; no son contenido de producción.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h852dp-xxhdpi")
class Beta035CaptureTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val output = File("build/capture-035")
    private val screen = mutableStateOf<@Composable () -> Unit>({})
    private val dark = mutableStateOf(false)
    private val venue = Venue("syn-1", "Salón de prueba", null, "Centro", 180, 0.0, 0, null, null)

    @Test
    fun captureBetaScreens() {
        assumeTrue("Captura desactivada", File(output, "enabled").exists())
        compose.setContent {
            VirGoTheme(darkTheme = dark.value) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { screen.value() }
            }
        }
        val shots: List<Pair<String, @Composable () -> Unit>> =
            listOf(
                "catalogo-vacio" to {
                    BetaCatalogContent(BetaCatalogState(loading = false), onRefresh = {}, onLogin = {}, onRegister = {})
                },
                "catalogo-error" to
                    { BetaCatalogContent(BetaCatalogState(loading = false, failed = true), onRefresh = {}) },
                "catalogo-datos" to {
                    BetaCatalogContent(
                        BetaCatalogState(loading = false, venues = listOf(venue, venue.copy(id = "syn-2"))),
                        {},
                    )
                },
                "secciones" to { BetaSectionsMenu(DemoRole.OWNER, onNavigate = {}) },
                "seccion-pendiente" to
                    { BetaSectionScreen(DemoRole.CLIENT, "studio", "Cuenta de prueba", "prueba@example.invalid") },
                "cuenta" to {
                    AccountProfileContent(
                        AccountProfileUiState(loading = false, profile = sampleUserProfile()),
                        SessionIdentity("Ana Lopez", "ana@mail.com", R.string.demo_role_client),
                        onRetry = {},
                    )
                },
                "espacio-proveedor" to {
                    RoleWorkspace(
                        DemoRole.PROVIDER,
                        "syn-provider",
                        onLogout = {},
                        "Cuenta de prueba",
                        "prueba@example.invalid",
                    )
                },
            )
        output.mkdirs()
        listOf(false, true).forEach { isDark ->
            shots.forEach { (name, content) ->
                compose.runOnIdle {
                    dark.value = isDark
                    screen.value = content
                }
                compose.waitForIdle()
                val file = File(output, "$name-${if (isDark) "oscuro" else "claro"}.png")
                // captureToImage espera foco de ventana, que Robolectric no concede: se dibuja la vista raíz.
                val root = compose.activity.window.decorView
                val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
                compose.runOnIdle { root.draw(Canvas(bitmap)) }
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
            }
        }
    }
}

private const val PNG_QUALITY = 100
