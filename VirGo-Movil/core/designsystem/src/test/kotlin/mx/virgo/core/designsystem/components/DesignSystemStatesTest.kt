package mx.virgo.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h852dp")
class DesignSystemStatesTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun topBarBackActionIsLabeledAndInvokesCallback() {
        var backClicks = 0
        compose.setContent {
            VirGoTheme {
                VirGoTopBar(
                    title = "Mi evento",
                    onBack = { backClicks++ },
                )
            }
        }

        compose
            .onNodeWithContentDescription("Regresar")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
            .performClick()

        compose.runOnIdle { assertEquals(1, backClicks) }
    }

    @Test
    fun bottomBarExposesSelectionAndInvokesItemClick() {
        var selectedIndex = -1
        compose.setContent {
            VirGoTheme {
                VirGoBottomBar(
                    items =
                        listOf(
                            VirGoBottomBarItem(
                                label = "Inicio",
                                icon = Icons.Filled.Home,
                                selected = true,
                            ),
                            VirGoBottomBarItem(
                                label = "Explorar",
                                icon = Icons.Filled.Place,
                                badgeCount = 3,
                            ),
                        ),
                    onItemClick = { selectedIndex = it },
                )
            }
        }

        compose
            .onNodeWithText("Inicio")
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHeightIsAtLeast(48.dp)
        compose.onNodeWithText("Explorar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, selectedIndex) }
        compose.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsMessageAndInvokesAction() {
        var actionClicks = 0
        compose.setContent {
            VirGoTheme {
                EmptyState(
                    title = "Sin favoritos",
                    text = "Guarda los salones que te gusten.",
                    action = {
                        VirGoOutlinedButton(onClick = { actionClicks++ }) {
                            Text("Explorar salones")
                        }
                    },
                )
            }
        }

        compose.onNodeWithText("Sin favoritos").assertIsDisplayed()
        compose.onNodeWithText("Guarda los salones que te gusten.").assertIsDisplayed()
        compose.onNodeWithText("Explorar salones").performClick()

        compose.runOnIdle { assertEquals(1, actionClicks) }
    }

    @Test
    fun loadingStateShowsProgressAndMessage() {
        compose.setContent {
            VirGoTheme {
                LoadingState(message = "Cargando salones…")
            }
        }

        compose.onNodeWithText("Cargando salones…").assertIsDisplayed()
        compose
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertExists()
    }

    @Test
    fun errorBannerIsLiveRegionAndRetryWorks() {
        var retries = 0
        compose.setContent {
            VirGoTheme {
                ErrorBanner(
                    message = "No pudimos cargar los salones.",
                    actionLabel = "Reintentar",
                    onAction = { retries++ },
                    modifier = Modifier.testTag("errorBanner"),
                )
            }
        }

        compose
            .onNodeWithTag("errorBanner")
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.LiveRegion,
                    LiveRegionMode.Polite,
                ),
            )
        compose
            .onNodeWithText("Reintentar")
            .assertHeightIsAtLeast(48.dp)
            .performClick()

        compose.runOnIdle { assertEquals(1, retries) }
    }
}
