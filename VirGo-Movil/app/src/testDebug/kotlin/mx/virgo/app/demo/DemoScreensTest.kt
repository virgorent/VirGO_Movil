package mx.virgo.app.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class DemoScreensTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun everyClientDestinationRenders() = assertDestinationsRender(DemoRole.CLIENT)

    @Test
    fun everyOwnerDestinationRenders() = assertDestinationsRender(DemoRole.OWNER)

    @Test
    fun everyProviderDestinationRenders() = assertDestinationsRender(DemoRole.PROVIDER)

    @Test
    fun chooserOpensEachRoleAndCanReturnWithoutAuthentication() {
        compose.setContent { VirGoTheme { VisualDemoApp(onExit = {}) } }
        listOf("Cliente", "Propietario de salón", "Proveedor").forEach { label ->
            compose.onNodeWithText(label).performScrollTo().performClick()
            compose.onNodeWithText("Cambiar rol").assertIsDisplayed().performClick()
        }
        compose.onNodeWithText("Cliente").assertIsDisplayed()
    }

    @Test
    fun demoChatExplainsThatMessagesAreSimulated() {
        compose.setContent { VirGoTheme { SharedDemoScreen(DemoRole.CLIENT, "chat", {}, {}) } }
        compose
            .onNodeWithText("Esta es una vista previa de conversación. No hay conexión con otras personas.")
            .assertIsDisplayed()
        compose.onNodeWithText("Añadir a la conversación demo").performScrollTo().assertIsDisplayed()
    }

    private fun assertDestinationsRender(role: DemoRole) {
        var route by mutableStateOf(role.home)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.3f)) {
                VirGoTheme(darkTheme = role == DemoRole.OWNER) {
                    key(route) { RenderDestination(role, route) }
                }
            }
        }
        DemoDestinations.forRole(role).forEach { destination ->
            compose.runOnIdle { route = destination.route }
            compose.waitForIdle()
            compose.onRoot().assertIsDisplayed()
            assertTrue(
                destination.route,
                compose
                    .onRoot()
                    .fetchSemanticsNode()
                    .children
                    .isNotEmpty(),
            )
        }
    }

    @Composable
    private fun RenderDestination(
        role: DemoRole,
        route: String,
    ) {
        if (DemoDestinations.shared.any { it.route == route }) {
            SharedDemoScreen(role, route, {}, {})
        } else {
            when (role) {
                DemoRole.CLIENT -> ClientDemoScreen(route, {}, {})
                DemoRole.OWNER -> OwnerDemoScreen(route, {}, {})
                DemoRole.PROVIDER -> ProviderDemoScreen(route, {}, {})
            }
        }
    }
}
