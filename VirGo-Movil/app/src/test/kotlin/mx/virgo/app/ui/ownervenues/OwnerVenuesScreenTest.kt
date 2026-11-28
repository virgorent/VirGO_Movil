package mx.virgo.app.ui.ownervenues

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueField
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.domain.VenueStep
import mx.virgo.core.testing.sampleVenueDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h1600dp")
class OwnerVenuesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val draft = OwnerVenue("v1", sampleVenueDraft(), VenueStatus.DRAFT)
    private val published = OwnerVenue("v2", sampleVenueDraft().copy(name = "Terraza Centro"), VenueStatus.PUBLISHED)

    @Test
    fun emptyListOffersToRegister() {
        var registered = false
        compose.setContent {
            VirGoTheme { OwnerVenuesContent(OwnerVenuesUiState(loading = false), {}, { registered = true }, {}) }
        }
        compose.onNodeWithText("Aún no registras salones").assertIsDisplayed()
        compose.onNodeWithText("Registrar salón").performClick()
        assertTrue(registered)
    }

    @Test
    fun listShowsStatusAndOpensAVenue() {
        var opened: OwnerVenue? = null
        compose.setContent {
            VirGoTheme {
                OwnerVenuesContent(
                    OwnerVenuesUiState(loading = false, venues = listOf(draft, published)),
                    {},
                    {},
                    { opened = it },
                )
            }
        }
        compose.onNodeWithText("Borrador").assertIsDisplayed()
        compose.onNodeWithText("Publicado").assertIsDisplayed()
        compose.onNodeWithText("Salón Las Palmas").performClick()
        assertEquals("v1", opened?.id)
    }

    @Test
    fun publishedOutcomeIsAnnounced() {
        compose.setContent {
            VirGoTheme {
                OwnerVenuesContent(
                    OwnerVenuesUiState(
                        loading = false,
                        venues = listOf(published),
                        outcome = VenueWizardOutcome.PUBLISHED,
                    ),
                    {},
                    {},
                    {},
                )
            }
        }
        compose.onNodeWithText("Tu salón ya aparece en el catálogo.").assertIsDisplayed()
    }

    @Test
    fun fieldErrorsAreShownAndAnnouncedNextToTheirField() {
        val state =
            VenueWizardUiState(
                key = "k",
                form = VenueForm(description = "corta"),
                fieldErrors = mapOf(VenueField.DESCRIPTION to mx.virgo.core.domain.FieldProblem.TOO_SHORT),
                error = WizardError.CHECK_FIELDS,
            )
        compose.setContent { VirGoTheme { VenueWizardContent(state, VenueWizardActions()) } }
        compose.onNodeWithText("Paso 1 de 3").assertIsDisplayed()
        compose.onNodeWithText("Revisa los campos marcados.").assertIsDisplayed()
        compose.onNodeWithText("Escribe al menos 20 caracteres.").performScrollTo().assertIsDisplayed()
        compose
            .onNodeWithContentDescription("Descripción")
            .assert(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.Error))
        compose.onNodeWithText("Las fotos se podrán agregar pronto.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reviewOffersEditPublishAndDraft() {
        val actions = mutableListOf<String>()
        val state = VenueWizardUiState(key = "k", step = VenueStep.REVIEW, form = VenueForm.from(draft), venueId = "v1")
        compose.setContent {
            VirGoTheme {
                VenueWizardContent(
                    state,
                    VenueWizardActions(
                        onGoTo = { actions += "edit:$it" },
                        onPublish = { actions += "publish" },
                        onSaveDraft = { actions += "draft" },
                    ),
                )
            }
        }
        compose.onNodeWithText("Paso 3 de 3").assertIsDisplayed()
        compose.onNodeWithText("Juriquilla").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1500 MXN por hora · mínimo 4 h").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Salón, Jardín").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Publicar").performScrollTo().performClick()
        compose.onNodeWithText("Guardar borrador").performScrollTo().performClick()
        compose.onAllNodesWithText("Editar")[0].performScrollTo().performClick()
        assertEquals(listOf("publish", "draft", "edit:DETAILS"), actions)
    }
}
