package mx.virgo.app.ui.ownervenues

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.FieldProblem
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.PublishVenueUseCase
import mx.virgo.core.domain.SaveVenueDraftUseCase
import mx.virgo.core.domain.VenueDraftValidator
import mx.virgo.core.domain.VenueField
import mx.virgo.core.domain.VenueStep
import javax.inject.Inject

private const val STATE_KEY = "venue_wizard"
private val stateJson = Json { ignoreUnknownKeys = true }

/**
 * `key` identifica el registro abierto; `venueId` existe desde el primer guardado y evita
 * crear dos salones (022 RF-05). `servicesTouched` es falso al retomar, porque `mine` no
 * devuelve servicios y no se deben borrar los guardados.
 */
@Serializable
data class VenueWizardUiState(
    val key: String? = null,
    val step: VenueStep = VenueStep.DETAILS,
    val form: VenueForm = VenueForm(),
    val venueId: String? = null,
    val servicesTouched: Boolean = true,
    val fieldErrors: Map<VenueField, FieldProblem> = emptyMap(),
    val busy: Boolean = false,
    val error: WizardError? = null,
    val outcome: VenueWizardOutcome? = null,
)

/** Callbacks del asistente, agrupados para que el contenido se pruebe sin `ViewModel`. */
data class VenueWizardActions(
    val onChange: ((VenueForm) -> VenueForm) -> Unit = {},
    val onGoTo: (VenueStep) -> Unit = {},
    val onNext: () -> Unit = {},
    val onSaveDraft: () -> Unit = {},
    val onPublish: () -> Unit = {},
    val onCancel: () -> Unit = {},
)

/** Errores generales del asistente; los de campo van en `fieldErrors`. */
enum class WizardError {
    CHECK_FIELDS,
    NETWORK,
    FORBIDDEN,
    NOT_FOUND,
    SERVER,
}

@HiltViewModel
class VenueWizardViewModel
    @Inject
    constructor(
        private val savedState: SavedStateHandle,
        private val saveDraft: SaveVenueDraftUseCase,
        private val publishVenue: PublishVenueUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState =
            MutableStateFlow(
                savedState.get<String>(STATE_KEY)?.let {
                    runCatching { stateJson.decodeFromString<VenueWizardUiState>(it) }.getOrNull()
                } ?: VenueWizardUiState(),
            )
        val state: StateFlow<VenueWizardUiState> = mutableState.asStateFlow()

        /** Abre un registro nuevo o un salón existente; no reinicia si ya está abierto (rotación). */
        fun start(
            key: String,
            venue: OwnerVenue?,
        ) {
            if (mutableState.value.key == key) return
            emit(
                VenueWizardUiState(
                    key = key,
                    form = venue?.let(VenueForm::from) ?: VenueForm(),
                    venueId = venue?.id,
                    servicesTouched = venue == null,
                ),
            )
        }

        fun update(change: (VenueForm) -> VenueForm) {
            val current = mutableState.value
            val form = change(current.form)
            emit(
                current.copy(
                    form = form,
                    servicesTouched = current.servicesTouched || form.services != current.form.services,
                    fieldErrors = current.fieldErrors.filterKeys { it !in changedFields(current.form, form) },
                    error = null,
                ),
            )
        }

        /** «Atrás» y «Editar» del resumen; no valida porque no guarda. */
        fun goTo(step: VenueStep) = emit(mutableState.value.copy(step = step, error = null))

        /** P3 → P4 valida localmente; P4 → P5 además guarda (el `POST` exige capacidad y precio). */
        fun next() {
            val current = mutableState.value
            if (!passes(current.step)) return
            when (current.step) {
                VenueStep.DETAILS -> emit(current.copy(step = VenueStep.CAPACITY_PRICE, fieldErrors = emptyMap()))
                VenueStep.CAPACITY_PRICE -> persist { emit(mutableState.value.copy(step = VenueStep.REVIEW)) }
                VenueStep.REVIEW -> Unit
            }
        }

        fun saveAsDraft() {
            if (passes(VenueStep.REVIEW)) {
                persist {
                    emit(mutableState.value.copy(busy = false, outcome = VenueWizardOutcome.SAVED))
                }
            }
        }

        fun publish() {
            if (!passes(VenueStep.REVIEW)) return
            persist { id ->
                when (val result = publishVenue(id)) {
                    is AppResult.Success ->
                        emit(
                            mutableState.value.copy(busy = false, outcome = VenueWizardOutcome.PUBLISHED),
                        )
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        /** Guarda (crea o actualiza) y continúa con el `id` ya conservado en el estado. */
        private fun persist(then: suspend (String) -> Unit) {
            val current = mutableState.value
            if (current.busy) return
            emit(current.copy(busy = true, error = null, fieldErrors = emptyMap()))
            viewModelScope.launch {
                when (val saved = saveDraft(current.venueId, current.form.toDraft(), current.servicesTouched)) {
                    is AppResult.Success -> {
                        emit(mutableState.value.copy(venueId = saved.value.id, servicesTouched = false))
                        then(saved.value.id)
                        if (mutableState.value.busy) emit(mutableState.value.copy(busy = false))
                    }
                    is AppResult.Failure -> fail(saved.error)
                }
            }
        }

        private suspend fun fail(error: AppError) {
            val current = mutableState.value.copy(busy = false)
            when (error) {
                AppError.Unauthorized -> {
                    emit(current)
                    session.invalidate(SessionEndReason.REVOKED)
                }
                is AppError.Validation -> {
                    val fields = serverFields(error.fields)
                    val step = fields.keys.firstOrNull()?.let(VenueDraftValidator::stepOf) ?: current.step
                    emit(current.copy(fieldErrors = fields, step = step, error = WizardError.CHECK_FIELDS))
                }
                AppError.Network, AppError.Timeout -> emit(current.copy(error = WizardError.NETWORK))
                AppError.Forbidden -> emit(current.copy(error = WizardError.FORBIDDEN))
                AppError.NotFound -> emit(current.copy(error = WizardError.NOT_FOUND))
                else -> emit(current.copy(error = WizardError.SERVER))
            }
        }

        /** Valida el paso; si falla, muestra los errores y devuelve `false`. */
        private fun passes(step: VenueStep): Boolean {
            val rejected = rejectInvalid(mutableState.value, step) ?: return true
            emit(rejected)
            return false
        }

        private fun emit(state: VenueWizardUiState) {
            mutableState.value = state
            savedState[STATE_KEY] = stateJson.encodeToString(VenueWizardUiState.serializer(), state)
        }
    }

private val SERVER_FIELDS =
    mapOf(
        "name" to VenueField.NAME,
        "shortName" to VenueField.SHORT_NAME,
        "description" to VenueField.DESCRIPTION,
        "address" to VenueField.ADDRESS,
        "zone" to VenueField.ZONE,
        "capacity" to VenueField.CAPACITY,
        "price" to VenueField.PRICE_BASE,
        "price.base" to VenueField.PRICE_BASE,
        "price.unit" to VenueField.PRICE_UNIT,
        "price.minHours" to VenueField.MIN_HOURS,
        "categories" to VenueField.CATEGORIES,
        "amenities" to VenueField.AMENITIES,
    )

/** Claves de `ValidationProblem` del servidor → campos del asistente (022 RF-07). */
internal fun serverFields(fields: Map<String, List<String>>): Map<VenueField, FieldProblem> =
    fields.keys
        .mapNotNull { key ->
            SERVER_FIELDS[key] ?: VenueField.SERVICES.takeIf { key.startsWith("services") }
        }.associateWith { FieldProblem.INVALID }

private fun changedFields(
    before: VenueForm,
    after: VenueForm,
): Set<VenueField> =
    buildSet {
        if (before.name != after.name) add(VenueField.NAME)
        if (before.shortName != after.shortName) add(VenueField.SHORT_NAME)
        if (before.description != after.description) add(VenueField.DESCRIPTION)
        if (before.address != after.address) add(VenueField.ADDRESS)
        if (before.zone != after.zone) add(VenueField.ZONE)
        if (before.capacity != after.capacity) add(VenueField.CAPACITY)
        if (before.price != after.price) add(VenueField.PRICE_BASE)
        if (before.unit != after.unit) add(VenueField.PRICE_UNIT)
        if (before.minHours != after.minHours) add(VenueField.MIN_HOURS)
        if (before.categories != after.categories) add(VenueField.CATEGORIES)
        if (before.amenities != after.amenities) add(VenueField.AMENITIES)
        if (before.services != after.services) add(VenueField.SERVICES)
    }

/** Estado con los errores del paso, o `null` si el paso es válido. En P5 lleva al primer paso con error. */
internal fun rejectInvalid(
    state: VenueWizardUiState,
    step: VenueStep,
): VenueWizardUiState? {
    val errors = VenueDraftValidator.validate(state.form.toDraft(), step)
    if (errors.isEmpty()) return null
    val target = if (step == VenueStep.REVIEW) VenueDraftValidator.stepOf(errors.keys.first()) else step
    return state.copy(fieldErrors = errors, step = target, error = WizardError.CHECK_FIELDS)
}
