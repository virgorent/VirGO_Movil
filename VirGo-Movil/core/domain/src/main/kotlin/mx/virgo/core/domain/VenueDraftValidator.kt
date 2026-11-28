package mx.virgo.core.domain

import java.math.BigDecimal

enum class VenueField {
    NAME,
    SHORT_NAME,
    DESCRIPTION,
    ADDRESS,
    ZONE,
    CAPACITY,
    PRICE_BASE,
    PRICE_UNIT,
    MIN_HOURS,
    CATEGORIES,
    AMENITIES,
    SERVICES,
}

enum class FieldProblem {
    REQUIRED,
    TOO_SHORT,
    TOO_LONG,
    INVALID,
}

/** Pasos del asistente: P3 datos, P4 capacidad y precio, P5 revisar. */
enum class VenueStep {
    DETAILS,
    CAPACITY_PRICE,
    REVIEW,
}

/** Reglas de 022 RF-02/RF-03, iguales a las del servidor salvo precio > 0. */
object VenueDraftValidator {
    const val NAME_MAX = 100
    const val SHORT_NAME_MAX = 20
    const val DESCRIPTION_MIN = 20
    const val DESCRIPTION_MAX = 2000
    const val ADDRESS_MAX = 200
    const val SERVICE_MAX = 100

    fun validate(
        draft: VenueDraft,
        step: VenueStep,
    ): Map<VenueField, FieldProblem> =
        when (step) {
            VenueStep.DETAILS -> details(draft)
            VenueStep.CAPACITY_PRICE -> capacityAndPrice(draft)
            VenueStep.REVIEW -> details(draft) + capacityAndPrice(draft)
        }

    /** Paso del asistente donde se captura el campo. */
    fun stepOf(field: VenueField): VenueStep =
        when (field) {
            VenueField.NAME, VenueField.SHORT_NAME, VenueField.DESCRIPTION, VenueField.ADDRESS, VenueField.ZONE ->
                VenueStep.DETAILS
            else -> VenueStep.CAPACITY_PRICE
        }

    private fun details(draft: VenueDraft): Map<VenueField, FieldProblem> =
        buildMap {
            text(VenueField.NAME, draft.name, 1, NAME_MAX)
            text(VenueField.SHORT_NAME, draft.shortName, 1, SHORT_NAME_MAX)
            text(VenueField.DESCRIPTION, draft.description, DESCRIPTION_MIN, DESCRIPTION_MAX)
            text(VenueField.ADDRESS, draft.address, 1, ADDRESS_MAX)
            when (draft.zone) {
                null -> put(VenueField.ZONE, FieldProblem.REQUIRED)
                !in VenueCatalogs.zones -> put(VenueField.ZONE, FieldProblem.INVALID)
            }
        }

    private fun capacityAndPrice(draft: VenueDraft): Map<VenueField, FieldProblem> =
        buildMap {
            val price = draft.priceBase ?: BigDecimal.ZERO
            if ((draft.capacity ?: 0) < 1) put(VenueField.CAPACITY, FieldProblem.INVALID)
            if (price <= BigDecimal.ZERO) put(VenueField.PRICE_BASE, FieldProblem.INVALID)
            if ((draft.minHours ?: 0) < 1) put(VenueField.MIN_HOURS, FieldProblem.INVALID)
            if (!VenueCatalogs.categories.containsAll(draft.categories)) {
                put(VenueField.CATEGORIES, FieldProblem.INVALID)
            }
            if (!VenueCatalogs.amenities.containsAll(draft.amenities)) {
                put(VenueField.AMENITIES, FieldProblem.INVALID)
            }
            if (draft.services.any { it.isBlank() || it.trim().length > SERVICE_MAX }) {
                put(VenueField.SERVICES, FieldProblem.TOO_LONG)
            }
        }

    private fun MutableMap<VenueField, FieldProblem>.text(
        field: VenueField,
        value: String,
        min: Int,
        max: Int,
    ) {
        val length = value.trim().length
        when {
            length == 0 -> put(field, FieldProblem.REQUIRED)
            length < min -> put(field, FieldProblem.TOO_SHORT)
            length > max -> put(field, FieldProblem.TOO_LONG)
        }
    }
}
