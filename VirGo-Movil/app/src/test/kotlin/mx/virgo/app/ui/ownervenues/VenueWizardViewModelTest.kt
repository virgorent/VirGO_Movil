package mx.virgo.app.ui.ownervenues

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.FieldProblem
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.PublishVenueUseCase
import mx.virgo.core.domain.SaveVenueDraftUseCase
import mx.virgo.core.domain.VenueField
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.domain.VenueStep
import mx.virgo.core.testing.FakeOwnerVenuesRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleVenueDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class VenueWizardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOwnerVenuesRepository()
    private val store = FakeSessionTokenStore()
    private val session = SessionRepository(store, Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val saved = SavedStateHandle()

    private fun viewModel(handle: SavedStateHandle = saved) =
        VenueWizardViewModel(handle, SaveVenueDraftUseCase(repository), PublishVenueUseCase(repository), session)

    private val complete = VenueForm.from(OwnerVenue("ignored", sampleVenueDraft(), VenueStatus.DRAFT))

    private fun VenueWizardViewModel.fill(form: VenueForm = complete) {
        start("new", null)
        update { form }
    }

    @Test
    fun shortDescriptionOrMissingZoneBlocksTheFirstStep() {
        val vm = viewModel()
        vm.fill(complete.copy(description = "a".repeat(19), zone = null))
        vm.next()
        val state = vm.state.value
        assertEquals(VenueStep.DETAILS, state.step)
        assertEquals(FieldProblem.TOO_SHORT, state.fieldErrors[VenueField.DESCRIPTION])
        assertEquals(FieldProblem.REQUIRED, state.fieldErrors[VenueField.ZONE])
        assertEquals(WizardError.CHECK_FIELDS, state.error)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun editingAFieldClearsOnlyItsError() {
        val vm = viewModel()
        vm.fill(complete.copy(name = "", zone = null))
        vm.next()
        vm.update { it.copy(name = "Nuevo nombre") }
        assertFalse(VenueField.NAME in vm.state.value.fieldErrors)
        assertTrue(VenueField.ZONE in vm.state.value.fieldErrors)
    }

    @Test
    fun firstSaveCreatesOnceAndLaterSavesPatch() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            assertEquals(VenueStep.CAPACITY_PRICE, vm.state.value.step)
            assertEquals(0, repository.createCalls)
            vm.next()
            assertEquals(VenueStep.REVIEW, vm.state.value.step)
            assertEquals("venue-1", vm.state.value.venueId)
            vm.goTo(VenueStep.CAPACITY_PRICE)
            vm.next()
            vm.saveAsDraft()
            assertEquals(1, repository.createCalls)
            assertEquals(2, repository.updateCalls)
            assertEquals(1, repository.venues.size)
            assertEquals(VenueWizardOutcome.SAVED, vm.state.value.outcome)
            assertEquals(VenueStatus.DRAFT, repository.venues.single().status)
        }

    @Test
    fun publishSavesThenPublishes() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            vm.next()
            vm.publish()
            assertEquals(VenueWizardOutcome.PUBLISHED, vm.state.value.outcome)
            assertEquals(1, repository.publishCalls)
            assertEquals(VenueStatus.PUBLISHED, repository.venues.single().status)
            assertFalse(vm.state.value.busy)
        }

    @Test
    fun networkFailureKeepsDataAndRetryDoesNotDuplicate() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            vm.next()
            repository.publishFailure = AppResult.Failure(AppError.Network)
            vm.publish()
            val failed = vm.state.value
            assertEquals(WizardError.NETWORK, failed.error)
            assertEquals(complete, failed.form)
            assertNull(failed.outcome)
            repository.publishFailure = null
            vm.publish()
            assertEquals(VenueWizardOutcome.PUBLISHED, vm.state.value.outcome)
            assertEquals(1, repository.createCalls)
            assertEquals(1, repository.venues.size)
        }

    @Test
    fun createFailureKeepsNoIdSoRetryCreates() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            repository.createFailure = AppResult.Failure(AppError.Timeout)
            vm.next()
            assertEquals(VenueStep.CAPACITY_PRICE, vm.state.value.step)
            assertEquals(WizardError.NETWORK, vm.state.value.error)
            assertNull(vm.state.value.venueId)
            repository.createFailure = null
            vm.next()
            assertEquals(VenueStep.REVIEW, vm.state.value.step)
        }

    @Test
    fun serverValidationReturnsToTheFieldStep() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            vm.next()
            repository.updateFailure = AppResult.Failure(AppError.Validation(mapOf("zone" to listOf("x"))))
            vm.saveAsDraft()
            assertEquals(VenueStep.DETAILS, vm.state.value.step)
            assertEquals(FieldProblem.INVALID, vm.state.value.fieldErrors[VenueField.ZONE])
        }

    @Test
    fun reviewWithBrokenDataJumpsToTheFirstBrokenStep() {
        val vm = viewModel()
        vm.fill(complete.copy(capacity = "0"))
        vm.goTo(VenueStep.REVIEW)
        vm.publish()
        assertEquals(VenueStep.CAPACITY_PRICE, vm.state.value.step)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun forbiddenAndServerErrorsAreShown() =
        runTest {
            val vm = viewModel()
            vm.fill()
            vm.next()
            repository.createFailure = AppResult.Failure(AppError.Forbidden)
            vm.next()
            assertEquals(WizardError.FORBIDDEN, vm.state.value.error)
            repository.createFailure = AppResult.Failure(AppError.Server("t"))
            vm.next()
            assertEquals(WizardError.SERVER, vm.state.value.error)
        }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            val vm = viewModel()
            vm.fill()
            vm.next()
            repository.createFailure = AppResult.Failure(AppError.Unauthorized)
            vm.next()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }

    @Test
    fun resumingADraftPatchesWithoutServicesUntilTheyChange() =
        runTest {
            val existing = OwnerVenue("v9", sampleVenueDraft().copy(services = emptyList()), VenueStatus.DRAFT)
            repository.venues += existing
            val vm = viewModel()
            vm.start("edit-v9", existing)
            assertEquals("v9", vm.state.value.venueId)
            assertEquals("Juriquilla", vm.state.value.form.zone)
            vm.next()
            vm.next()
            assertEquals(false, repository.lastIncludeServices)
            vm.goTo(VenueStep.CAPACITY_PRICE)
            vm.update { it.copy(services = "Meseros") }
            vm.next()
            assertEquals(true, repository.lastIncludeServices)
            assertEquals(0, repository.createCalls)
        }

    @Test
    fun stateSurvivesRecreationAndStartIsIgnoredForTheSameKey() {
        val vm = viewModel()
        vm.fill(complete.copy(name = "Guardado"))
        vm.next()
        val restored = viewModel(SavedStateHandle(mapOf("venue_wizard" to saved.get<String>("venue_wizard"))))
        restored.start("new", null)
        assertEquals("Guardado", restored.state.value.form.name)
        assertEquals(VenueStep.CAPACITY_PRICE, restored.state.value.step)
        restored.start("other", null)
        assertEquals("", restored.state.value.form.name)
    }

    @Test
    fun formParsesNumbersStrictly() {
        val draft = complete.copy(capacity = "12a", price = "1500,5", minHours = "").toDraft()
        assertNull(draft.capacity)
        assertEquals(0, java.math.BigDecimal("1500.5").compareTo(draft.priceBase))
        assertNull(draft.minHours)
        assertNull(complete.copy(price = "1.234").toDraft().priceBase)
        assertEquals(
            listOf("Mobiliario", "Meseros"),
            complete.copy(services = " Mobiliario, ,Meseros ").toDraft().services,
        )
    }

    @Test
    fun serverKeysMapToWizardFields() {
        val fields =
            serverFields(
                mapOf(
                    "price.base" to emptyList(),
                    "price.minHours" to emptyList(),
                    "services[0].name" to emptyList(),
                    "body" to emptyList(),
                ),
            )
        assertEquals(setOf(VenueField.PRICE_BASE, VenueField.MIN_HOURS, VenueField.SERVICES), fields.keys)
    }
}
