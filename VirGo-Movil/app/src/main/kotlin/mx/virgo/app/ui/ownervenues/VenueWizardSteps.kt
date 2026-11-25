package mx.virgo.app.ui.ownervenues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.FieldProblem
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.VenueCatalogs
import mx.virgo.core.domain.VenueField
import mx.virgo.core.domain.VenueStep

private val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)
private val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)

/** P3: datos del salón (022 RF-02, RF-09). */
@Composable
internal fun DetailsStep(
    form: VenueForm,
    errors: Map<VenueField, FieldProblem>,
    onChange: ((VenueForm) -> VenueForm) -> Unit,
) {
    LabeledField(
        R.string.venue_field_name,
        form.name,
        errors[VenueField.NAME],
        VenueField.NAME,
        null,
    ) { value -> onChange { it.copy(name = value) } }
    LabeledField(
        R.string.venue_field_short_name,
        form.shortName,
        errors[VenueField.SHORT_NAME],
        VenueField.SHORT_NAME,
        R.string.venue_field_short_name_hint,
    ) { value -> onChange { it.copy(shortName = value) } }
    LabeledField(
        R.string.venue_field_description,
        form.description,
        errors[VenueField.DESCRIPTION],
        VenueField.DESCRIPTION,
        R.string.venue_field_description_hint,
        singleLine = false,
    ) { value -> onChange { it.copy(description = value) } }
    LabeledField(
        R.string.venue_field_address,
        form.address,
        errors[VenueField.ADDRESS],
        VenueField.ADDRESS,
        null,
    ) { value -> onChange { it.copy(address = value) } }
    ChoiceGroup(
        label = R.string.venue_field_zone,
        options = VenueCatalogs.zones,
        selected = setOfNotNull(form.zone),
        error = errors[VenueField.ZONE]?.let { R.string.venue_error_zone },
        labelOf = { it },
    ) { zone -> onChange { it.copy(zone = zone) } }
    VirGoRow(
        title = stringResource(R.string.venue_wizard_photos_soon),
        icon = Icons.Outlined.Info,
        tone = VirGoTone.GRAY,
    )
}

/** P4: capacidad, precio, categorías, amenidades y servicios (022 RF-03). */
@Composable
internal fun CapacityStep(
    form: VenueForm,
    errors: Map<VenueField, FieldProblem>,
    onChange: ((VenueForm) -> VenueForm) -> Unit,
) {
    LabeledField(
        R.string.venue_field_capacity,
        form.capacity,
        errors[VenueField.CAPACITY],
        VenueField.CAPACITY,
        null,
        keyboard = numberKeyboard,
    ) { value -> onChange { it.copy(capacity = value) } }
    LabeledField(
        R.string.venue_field_price,
        form.price,
        errors[VenueField.PRICE_BASE],
        VenueField.PRICE_BASE,
        null,
        keyboard = decimalKeyboard,
    ) { value -> onChange { it.copy(price = value) } }
    ChoiceGroup(
        label = R.string.venue_field_unit,
        options = PriceUnit.entries.map { it.wire },
        selected = setOf(form.unit),
        error = errors[VenueField.PRICE_UNIT]?.let { R.string.venue_error_invalid },
        labelOf = {
            stringResource(
                if (it ==
                    PriceUnit.DAY.wire
                ) {
                    R.string.venue_unit_day
                } else {
                    R.string.venue_unit_hour
                },
            )
        },
    ) { unit -> onChange { it.copy(unit = unit) } }
    LabeledField(
        R.string.venue_field_min_hours,
        form.minHours,
        errors[VenueField.MIN_HOURS],
        VenueField.MIN_HOURS,
        null,
        keyboard = numberKeyboard,
    ) { value -> onChange { it.copy(minHours = value) } }
    ListsFields(form, errors, onChange)
}

/** Parte de P4 con listas cerradas y servicios. */
@Composable
private fun ListsFields(
    form: VenueForm,
    errors: Map<VenueField, FieldProblem>,
    onChange: ((VenueForm) -> VenueForm) -> Unit,
) {
    ChoiceGroup(
        label = R.string.venue_field_categories,
        options = VenueCatalogs.categories,
        selected = form.categories.toSet(),
        error = errors[VenueField.CATEGORIES]?.let { R.string.venue_error_invalid },
        labelOf = { categoryLabel(it) },
    ) { value -> onChange { it.copy(categories = it.categories.toggle(value)) } }
    ChoiceGroup(
        label = R.string.venue_field_amenities,
        options = VenueCatalogs.amenities,
        selected = form.amenities.toSet(),
        error = errors[VenueField.AMENITIES]?.let { R.string.venue_error_invalid },
        labelOf = { it },
    ) { value -> onChange { it.copy(amenities = it.amenities.toggle(value)) } }
    LabeledField(
        R.string.venue_field_services,
        form.services,
        errors[VenueField.SERVICES],
        VenueField.SERVICES,
        R.string.venue_field_services_hint,
    ) { value -> onChange { it.copy(services = value) } }
}

/** P5: resumen con «Editar» por paso (022 RF-04). */
@Composable
internal fun ReviewStep(
    form: VenueForm,
    onGoTo: (VenueStep) -> Unit,
) {
    val draft = form.toDraft()
    ReviewSection(R.string.venue_wizard_step_details, { onGoTo(VenueStep.DETAILS) }) {
        ReviewRow(R.string.venue_field_name, draft.name)
        ReviewRow(R.string.venue_field_short_name, draft.shortName)
        ReviewRow(R.string.venue_field_description, draft.description)
        ReviewRow(R.string.venue_field_address, draft.address)
        ReviewRow(R.string.venue_field_zone, draft.zone.orEmpty())
    }
    val unit =
        stringResource(
            if (draft.priceUnit ==
                PriceUnit.DAY
            ) {
                R.string.venue_unit_day
            } else {
                R.string.venue_unit_hour
            },
        )
    val none = stringResource(R.string.venue_review_none)
    ReviewSection(R.string.venue_wizard_step_capacity, { onGoTo(VenueStep.CAPACITY_PRICE) }) {
        ReviewRow(R.string.venue_field_capacity, draft.capacity?.toString().orEmpty())
        ReviewRow(
            R.string.venue_field_price,
            stringResource(R.string.venue_review_price, form.price, unit.lowercase(), draft.minHours ?: 0),
        )
        ReviewRow(R.string.venue_field_categories, form.categories.map { categoryLabel(it) }.joinOrNone(none))
        ReviewRow(R.string.venue_field_amenities, form.amenities.joinOrNone(none))
        ReviewRow(R.string.venue_field_services, draft.services.joinOrNone(none))
    }
}

@Composable
private fun ReviewSection(
    title: Int,
    onEdit: () -> Unit,
    content: @Composable () -> Unit,
) {
    VirGoSection(
        title = stringResource(title),
        action = { TextButton(onClick = onEdit) { Text(stringResource(R.string.venue_wizard_edit)) } },
    ) {
        VirGoCard { Column(Modifier.padding(horizontal = 16.dp)) { content() } }
    }
}

@Composable
private fun ReviewRow(
    label: Int,
    value: String,
) {
    // Sin límite de líneas: el resumen debe mostrar completo lo que se va a publicar.
    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
    VirGoRowDivider()
}

private fun List<String>.toggle(value: String): List<String> = if (value in this) this - value else this + value

private fun List<String>.joinOrNone(none: String): String = if (isEmpty()) none else joinToString(", ")

@Composable
private fun categoryLabel(value: String): String =
    stringResource(
        when (value) {
            "Salon" -> R.string.venue_category_salon
            "Jardin" -> R.string.venue_category_jardin
            "Terraza" -> R.string.venue_category_terraza
            "Hacienda" -> R.string.venue_category_hacienda
            "Rooftop" -> R.string.venue_category_rooftop
            else -> R.string.venue_category_salon_eventos
        },
    )
