package mx.virgo.app.demo

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h800dp")
class WorkspaceInteractionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun eventSheetUpdatesTheBackgroundNameBudgetAndGuestCount() {
        val workspace = WorkspaceViewModel()
        var editing by mutableStateOf(true)
        compose.setContent {
            VirGoTheme {
                CompositionLocalProvider(LocalWorkspace provides workspace) {
                    if (editing) {
                        DemoPage("Formulario") { DemoSheetsEvent { editing = false } }
                    } else {
                        ClientDemoScreen("event", {}, {})
                    }
                }
            }
        }
        replace(R.string.demo_sheet_name, "Fiesta local")
        replace(R.string.demo_sheet_amount, "25000")
        click(R.string.demo_sheet_confirm)
        compose.onNodeWithText(text(R.string.demo_client_event_name_label)).assertTextContains("Fiesta local")
        compose.runOnIdle {
            val event = workspace.uiState.value.latest(WorkspaceSection.EVENT)
            assertEquals("Fiesta local", event?.name)
            assertEquals(EVENT_AMOUNT, event?.amount)
            assertEquals(SHEET_MIN_GUESTS, event?.guests)
        }
    }

    @Test
    fun paymentSheetAddsALocalLedgerEntryToTheBackground() {
        val workspace = WorkspaceViewModel()
        var editing by mutableStateOf(true)
        compose.setContent {
            VirGoTheme {
                CompositionLocalProvider(LocalWorkspace provides workspace) {
                    if (editing) {
                        DemoPage("Formulario") { DemoSheetsPayment { editing = false } }
                    } else {
                        ClientDemoScreen("payments", {}, {})
                    }
                }
            }
        }
        replace(R.string.demo_sheet_concept, "Anticipo local")
        replace(R.string.demo_sheet_amount, "1500")
        click(R.string.demo_sheet_confirm)
        compose.onNodeWithText("Anticipo local").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.workspace_local_changes)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun filtersSheetChangesTheBackgroundSearchField() {
        val workspace = WorkspaceViewModel()
        var editing by mutableStateOf(true)
        compose.setContent {
            VirGoTheme {
                CompositionLocalProvider(LocalWorkspace provides workspace) {
                    if (editing) {
                        DemoPage("Formulario") { DemoSheetsSearch { editing = false } }
                    } else {
                        ClientDemoScreen("explore", {}, {})
                    }
                }
            }
        }
        replace(R.string.demo_sheet_query, "Terraza")
        click(R.string.demo_sheet_confirm)
        compose.onNodeWithText(text(R.string.demo_client_search)).performScrollTo().assertTextContains("Terraza")
    }

    @Test
    fun realProfileSeedSurvivesLocalSheetEditWithoutChangingAccountIdentity() {
        val workspace = WorkspaceViewModel()
        workspace.initializeProfile("Nombre real", "real@example.invalid")
        var editing by mutableStateOf(true)
        compose.setContent {
            VirGoTheme {
                CompositionLocalProvider(LocalWorkspace provides workspace) {
                    if (editing) {
                        DemoPage("Formulario") { DemoSheetsProfile { editing = false } }
                    } else {
                        SharedDemoScreen(DemoRole.CLIENT, "profile", {}, {})
                    }
                }
            }
        }
        replace(R.string.demo_sheet_alias, "Alias local")
        click(R.string.demo_sheet_confirm)
        compose.onNodeWithText(text(R.string.demo_name_label)).performScrollTo().assertTextContains("Alias local")
        compose.runOnIdle {
            assertEquals(
                "Nombre real",
                workspace.uiState.value.accountProfile
                    ?.name,
            )
            assertEquals(
                "real@example.invalid",
                workspace.uiState.value.accountProfile
                    ?.email,
            )
        }
    }

    @Test
    fun switchingAccountsNeverDisplaysTheOtherAccountsSheetRecord() {
        val first = WorkspaceViewModel()
        val second = WorkspaceViewModel()
        first.confirm(WorkspaceSection.PAYMENTS, WorkspaceRecord(name = "Solo primera", summary = "Local"))
        var workspace by mutableStateOf(first)
        compose.setContent {
            VirGoTheme {
                CompositionLocalProvider(LocalWorkspace provides workspace) {
                    ClientDemoScreen("payments", {}, {})
                }
            }
        }
        compose.onNodeWithText("Solo primera").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { workspace = second }
        compose.onAllNodesWithText("Solo primera").assertCountEquals(0)
        compose.runOnIdle { assertNull(second.uiState.value.latest(WorkspaceSection.PAYMENTS)) }
    }

    private fun replace(
        resource: Int,
        value: String,
    ) {
        compose.onNodeWithText(text(resource)).performScrollTo().performTextReplacement(value)
    }

    private fun click(resource: Int) {
        compose.onNodeWithText(text(resource)).performScrollTo().performClick()
    }

    private fun text(resource: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resource)
}

private const val EVENT_AMOUNT = 25_000.0
