package mx.virgo.app.ui.events

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.input.ImeAction
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.EventField
import mx.virgo.core.domain.EventProblem
import mx.virgo.core.domain.GuestField
import mx.virgo.core.testing.FakeEventsRepository
import mx.virgo.core.testing.sampleEvent
import mx.virgo.core.testing.sampleGuest
import mx.virgo.core.testing.sampleTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h2000dp")
class EventScreensTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.parse("2027-05-10")

    @Test
    fun emptyListOffersToCreate() {
        var created = 0
        compose.setContent {
            VirGoTheme { EventsListContent(EventsUiState(loading = false), {}, { created++ }, {}) }
        }
        compose.onNodeWithText("Aún no tienes eventos").assertIsDisplayed()
        compose.onNodeWithText("Crear evento").performClick()
        assertEquals(1, created)
    }

    @Test
    fun detailShowsCountdownDataAndAccesses() {
        val taps = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                EventDetailBody(
                    EventDetailState(sampleEvent().id, loading = false, event = sampleEvent()),
                    EventDetailActions(onGuests = { taps += "guests" }, onTasks = { taps += "tasks" }),
                    today,
                )
            }
        }
        compose.onNodeWithText("Faltan 5 días").assertIsDisplayed()
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
        compose.onNodeWithText("150,000", substring = true).assertIsDisplayed()
        compose.onNode(hasText("Invitados") and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
        compose.onNode(hasText("Pendientes") and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf("guests", "tasks"), taps)
    }

    @Test
    fun formShowsErrorsKeptVenueAndDisablesWhileSaving() {
        compose.setContent {
            VirGoTheme {
                EventFormContent(
                    EventFormUiState(
                        eventId = "e1",
                        original = sampleEvent(),
                        fieldErrors = mapOf(EventField.DATE to EventProblem.PAST_DATE),
                        saving = true,
                    ),
                    EventFormActions(),
                )
            }
        }
        compose.onNodeWithText("Editar evento").assertIsDisplayed()
        compose.onNodeWithText("La fecha no puede estar en el pasado.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("El salón Salón Las Palmas se conserva.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Guardando…").performScrollTo().assertIsNotEnabled()
        // Los campos de números pasan al siguiente con el teclado; el presupuesto cierra el formulario.
        compose.onNode(hasContentDescription("Invitados") and hasImeAction(ImeAction.Next)).assertExists()
        compose.onNode(hasContentDescription("Horas") and hasImeAction(ImeAction.Next)).assertExists()
        compose.onNode(hasContentDescription("Presupuesto en pesos") and hasImeAction(ImeAction.Done)).assertExists()
    }

    @Test
    fun guestsShowTotalsAndAskBeforeDeleting() {
        val taps = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                GuestsContent(
                    GuestsUiState(
                        eventId = "e1",
                        loading = false,
                        guests = listOf(sampleGuest()),
                        deleting = sampleGuest(),
                    ),
                    "Boda",
                    GuestsActions(onConfirmDelete = { taps += it.id }, onShare = { taps += "share" }),
                )
            }
        }
        compose.onNodeWithText("Confirmados: 2 · Pendientes: 0 · No asisten: 0").assertIsDisplayed()
        compose.onNodeWithText("Confirmado").assertIsDisplayed()
        compose.onNodeWithText("¿Eliminar a María Pérez?").assertIsDisplayed()
        compose.onAllNodes(hasText("Eliminar") and hasClickAction()).onLast().performClick()
        assertEquals(listOf(sampleGuest().id), taps)
    }

    @Test
    fun guestFormShowsThePhoneError() {
        compose.setContent {
            VirGoTheme {
                GuestsContent(
                    GuestsUiState(
                        eventId = "e1",
                        loading = false,
                        editor = GuestEditor(errors = setOf(GuestField.PHONE)),
                    ),
                    "Boda",
                    GuestsActions(),
                )
            }
        }
        compose.onNodeWithText("Escribe el teléfono, hasta 16 caracteres.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tasksAreGroupedAndToggle() {
        val toggled = mutableListOf<String>()
        val late = sampleTask().copy(id = "late", title = "Pagar anticipo", dueDate = today.minusDays(1))
        val done = sampleTask().copy(id = "done", title = "Elegir salón", done = true)
        compose.setContent {
            VirGoTheme {
                TasksContent(
                    TasksUiState(eventId = "e1", loading = false, tasks = listOf(late, sampleTask(), done)),
                    today,
                    TasksActions(onToggle = { toggled += it.id }),
                )
            }
        }
        compose.onNodeWithText("Vencidas").assertIsDisplayed()
        compose.onNodeWithText("Hechas").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Contratar fotógrafo").performScrollTo().assertIsDisplayed()
        assertEquals(emptyList<String>(), toggled)
        // Cada casilla lleva el nombre de su pendiente para TalkBack.
        compose
            .onNode(
                hasContentDescription("Pagar anticipo") and isToggleable(),
                useUnmergedTree = true,
            ).performClick()
        assertEquals(listOf("late"), toggled)
    }

    @Test
    fun clientEventsOpensTheRequestedStep() {
        val repository = FakeEventsRepository().apply { events += sampleEvent() }
        val vm = EventsViewModel(repository, SessionRepository(FakeSessionTokenStore(), Clock.systemUTC()))
        var leftWorkspace = false
        compose.setContent {
            VirGoTheme {
                ClientEvents(
                    openStep = EventStep.GUESTS,
                    onBack = { leftWorkspace = true },
                    viewModel = vm,
                    guests = { event, back -> TextButton(onClick = back) { Text("Invitados de ${event.name}") } },
                )
            }
        }
        compose.onNodeWithText("Boda de Ana y Luis").performClick()
        compose.onNodeWithText("Invitados de Boda de Ana y Luis").assertIsDisplayed().performClick()
        compose.onNodeWithText("Editar evento").performScrollTo().assertIsDisplayed()
        // La flecha del encabezado deshace cada paso antes de salir de «Mi Evento».
        compose.onNodeWithText("Mi Evento").assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Editar evento").assertDoesNotExist()
        assertFalse(leftWorkspace)
        compose.onNodeWithContentDescription("Volver").performClick()
        assertTrue(leftWorkspace)
    }
}
