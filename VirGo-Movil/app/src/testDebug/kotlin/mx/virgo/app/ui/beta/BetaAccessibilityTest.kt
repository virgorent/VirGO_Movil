package mx.virgo.app.ui.beta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import mx.virgo.app.R
import mx.virgo.app.demo.DemoDestinations
import mx.virgo.app.demo.DemoRole
import mx.virgo.app.demo.RoleWorkspace
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Venue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val MIN_TARGET_DP = 48f
private const val COMPACT_WIDTH_DP = 320f
private const val DOUBLE_FONT_SCALE = 2f
private const val PIXEL_TOLERANCE = 1f
private const val DP_TOLERANCE = 0.5f
private const val SESSION_NAME = "Session name"
private const val SESSION_EMAIL = "session@example.invalid"

/**
 * T014 — revisión integral automatizada de las pantallas beta.
 *
 * Barre cada pantalla a 320 dp con fuente 2.0 en ambos temas y comprueba que
 * ningún texto se recorte ni use elipsis, que todo control tocable mida al menos
 * 48×48 dp y que toda imagen expuesta tenga descripción. La altura de 2000 dp
 * compone todas las filas perezosas; no sustituye la revisión manual con TalkBack.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h2000dp")
class BetaAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    private val screenHolder = mutableStateOf<@Composable () -> Unit>({})
    private val darkHolder = mutableStateOf(false)
    private var started = false

    private val venue =
        Venue("real-id", "Salón con un nombre bastante largo", null, "Centro histórico", 180, 0.0, 0, null, null)

    @Test
    fun catalogStatesAreAccessibleInLightTheme() = assertCatalogStates(darkTheme = false)

    @Test
    fun catalogStatesAreAccessibleInDarkTheme() = assertCatalogStates(darkTheme = true)

    @Test
    fun sectionsAndAccountAreAccessibleInLightTheme() = assertSections(darkTheme = false)

    @Test
    fun sectionsAndAccountAreAccessibleInDarkTheme() = assertSections(darkTheme = true)

    @Test
    fun unavailableRecoveryIsAccessibleInBothThemes() {
        listOf(false, true).forEach { dark ->
            sweep("recover dark=$dark", dark) { BetaUnavailableScreen(R.string.beta_recovery, onBack = {}) }
        }
    }

    @Test
    fun ownerAndProviderWorkspacesAreAccessibleInBothThemes() {
        listOf(DemoRole.OWNER, DemoRole.PROVIDER).forEach { role ->
            listOf(false, true).forEach { dark ->
                sweep("workspace $role dark=$dark", dark) {
                    RoleWorkspace(role, "synthetic-$role-$dark", onLogout = {}, SESSION_NAME, SESSION_EMAIL)
                }
                compose.onNode(hasText("Secciones") and hasClickAction()).performClick()
                compose.waitForIdle()
                assertAccessible("sections menu $role dark=$dark")
            }
        }
    }

    private fun assertCatalogStates(darkTheme: Boolean) {
        val states =
            mapOf(
                "loading" to BetaCatalogState(),
                "empty" to BetaCatalogState(loading = false),
                "error" to BetaCatalogState(loading = false, failed = true),
                "data" to BetaCatalogState(loading = false, venues = listOf(venue, venue.copy(id = "b", capacity = 0))),
            )
        states.forEach { (name, state) ->
            sweep("catalog $name dark=$darkTheme", darkTheme) {
                BetaCatalogContent(state, onRefresh = {}, onLogin = {}, onRegister = {})
            }
        }
    }

    private fun assertSections(darkTheme: Boolean) {
        DemoRole.entries.forEach { role ->
            sweep("menu $role dark=$darkTheme", darkTheme) { BetaSectionsMenu(role, onNavigate = {}) }
            DemoDestinations.forRole(role).filter { it.route != "explore" }.forEach { destination ->
                sweep("${destination.route} $role dark=$darkTheme", darkTheme) {
                    BetaSectionScreen(role, destination.route, SESSION_NAME, SESSION_EMAIL)
                }
            }
        }
    }

    private fun sweep(
        label: String,
        darkTheme: Boolean,
        screen: @Composable () -> Unit,
    ) {
        screenHolder.value = screen
        darkHolder.value = darkTheme
        if (!started) {
            started = true
            compose.setContent {
                val density = LocalDensity.current.density
                CompositionLocalProvider(LocalDensity provides Density(density, fontScale = DOUBLE_FONT_SCALE)) {
                    VirGoTheme(darkTheme = darkHolder.value) { screenHolder.value() }
                }
            }
        }
        compose.waitForIdle()
        assertAccessible(label)
    }

    private fun assertAccessible(label: String) {
        val density = compose.density.density
        val root = compose.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        assertEquals("$label: viewport width", COMPACT_WIDTH_DP, root.size.width / density, PIXEL_TOLERANCE)
        val nodes = root.descendantsAndSelf()
        nodes.forEach { node ->
            assertTextFits(label, node)
            if (node.config.contains(SemanticsActions.OnClick)) assertTarget(label, node, density)
            if (node.config.getOrNull(SemanticsProperties.Role) == Role.Image) {
                assertTrue(
                    "$label: image without contentDescription",
                    !node.config.getOrNull(SemanticsProperties.ContentDescription).isNullOrEmpty(),
                )
            }
        }
        assertTrue("$label: sweep found no text", nodes.any { it.config.contains(SemanticsProperties.Text) })
    }
}

private fun SemanticsNode.descendantsAndSelf(): List<SemanticsNode> =
    listOf(this) + children.flatMap { it.descendantsAndSelf() }

private fun assertTextFits(
    label: String,
    node: SemanticsNode,
) {
    val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return
    val layouts = mutableListOf<TextLayoutResult>()
    action(layouts)
    layouts.forEach { layout ->
        val message = "$label: text must fit at 320 dp and font 2.0: «${layout.layoutInput.text.text}»"
        assertTrue(message, !layout.didOverflowHeight)
        assertTrue(message, !layout.multiParagraph.didExceedMaxLines)
        repeat(layout.lineCount) { line ->
            assertTrue(message, !layout.isLineEllipsized(line))
            assertTrue(message, layout.getLineRight(line) <= layout.size.width + PIXEL_TOLERANCE)
        }
    }
}

private fun assertTarget(
    label: String,
    node: SemanticsNode,
    density: Float,
) {
    // Off-viewport nodes report clipped (0 dp) touch bounds; the unclipped layout
    // size is the real target. Keep the larger of both to honour touch-target padding.
    val width = maxOf(node.touchBoundsInRoot.width, node.size.width.toFloat()) / density
    val height = maxOf(node.touchBoundsInRoot.height, node.size.height.toFloat()) / density
    val name =
        node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            ?: node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
            ?: "unnamed"
    assertTrue("$label: «$name» width $width dp < 48", width >= MIN_TARGET_DP - DP_TOLERANCE)
    assertTrue("$label: «$name» height $height dp < 48", height >= MIN_TARGET_DP - DP_TOLERANCE)
}
