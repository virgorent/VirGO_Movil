package mx.virgo.app.ui.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderField
import mx.virgo.core.domain.ProviderPackage
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.domain.VenueCatalogs

@Composable
fun ProviderProfile(viewModel: ProviderProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    ProviderProfileContent(
        state,
        ProviderProfileActions(
            onDraft = viewModel::edit,
            onAddPackage = viewModel::addPackage,
            onRemovePackage = viewModel::removePackage,
            onPackage = viewModel::editPackage,
            onSubmit = { publish -> if (publish) viewModel.publish() else viewModel.save() },
            onRetry = viewModel::refresh,
        ),
    )
}

@Composable
internal fun ProviderProfileContent(
    state: ProviderProfileUiState,
    actions: ProviderProfileActions,
) {
    VirGoPageBody {
        when (state.load) {
            Load.LOADING -> LoadingState()
            Load.UNAVAILABLE -> ComingSoon(R.string.provider_profile_unavailable)
            Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            Load.READY -> ProfileForm(state, actions)
        }
    }
}

@Composable
private fun ProfileForm(
    state: ProviderProfileUiState,
    actions: ProviderProfileActions,
) {
    val draft = state.draft
    Text(
        stringResource(if (state.published) R.string.provider_status_published else R.string.provider_status_draft),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
    LabeledField(
        R.string.provider_business_name,
        draft.businessName,
        state.missing.contains(ProviderField.BUSINESS_NAME),
    ) { actions.onDraft(draft.copy(businessName = it)) }
    Choice(R.string.provider_category, state.missing.contains(ProviderField.CATEGORY)) {
        ProviderCategory.entries.forEach { category ->
            FilterChip(
                selected = draft.category == category,
                onClick = { actions.onDraft(draft.copy(category = category)) },
                label = { Text(stringResource(categoryLabel(category))) },
            )
        }
    }
    LabeledField(
        R.string.provider_description,
        draft.description,
        state.missing.contains(ProviderField.DESCRIPTION),
        singleLine = false,
    ) { actions.onDraft(draft.copy(description = it)) }
    Choice(R.string.provider_pricing, state.missing.contains(ProviderField.PRICING)) {
        ProviderPricing.entries.forEach { pricing ->
            FilterChip(
                selected = draft.pricing == pricing,
                onClick = { actions.onDraft(draft.copy(pricing = pricing)) },
                label = { Text(pricingText(pricing)) },
            )
        }
    }
    LabeledField(
        R.string.provider_response_time,
        draft.responseTime,
        state.missing.contains(ProviderField.RESPONSE_TIME),
    ) { actions.onDraft(draft.copy(responseTime = it)) }
    Packages(draft.packages, state.missing.contains(ProviderField.PACKAGES), actions)
    ProfileFooter(state, actions)
}

@Composable
private fun ProfileFooter(
    state: ProviderProfileUiState,
    actions: ProviderProfileActions,
) {
    val draft = state.draft
    Choice(R.string.provider_zones, state.missing.contains(ProviderField.ZONES)) {
        VenueCatalogs.zones.forEach { zone ->
            FilterChip(
                selected = zone in draft.zones,
                onClick = { actions.onDraft(draft.toggleZone(zone)) },
                label = { Text(zone) },
            )
        }
    }
    state.notice?.let { Text(stringResource(noticeText(it)), color = noticeColor(it)) }
    VirGoButton(
        onClick = { actions.onSubmit(false) },
        enabled = !state.saving,
        text = stringResource(R.string.provider_save),
        modifier = Modifier.fillMaxWidth(),
    )
    if (!state.published) {
        VirGoButton(
            onClick = { actions.onSubmit(true) },
            enabled = !state.saving,
            text = stringResource(R.string.provider_publish),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Packages(
    packages: List<ProviderPackage>,
    missing: Boolean,
    actions: ProviderProfileActions,
) {
    VirGoSection(title = stringResource(R.string.provider_packages)) {
        if (missing) Text(stringResource(R.string.provider_missing_packages), color = MaterialTheme.colorScheme.error)
        packages.forEachIndexed { index, item ->
            VirGoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledField(R.string.provider_package_name, item.name, false) {
                        actions.onPackage(index, item.copy(name = it))
                    }
                    LabeledField(
                        R.string.provider_package_price,
                        item.price?.toPlainString().orEmpty(),
                        false,
                        keyboardType = KeyboardType.Number,
                    ) { typed ->
                        val digits = typed.filter(Char::isDigit).take(MAX_PRICE_DIGITS)
                        actions.onPackage(index, item.copy(price = digits.toBigDecimalOrNull()))
                    }
                    LabeledField(R.string.provider_package_includes, item.includes, false, singleLine = false) {
                        actions.onPackage(index, item.copy(includes = it))
                    }
                    TextButton(onClick = { actions.onRemovePackage(index) }) {
                        Text(stringResource(R.string.provider_package_remove))
                    }
                }
            }
        }
        TextButton(onClick = actions.onAddPackage) { Text(stringResource(R.string.provider_package_add)) }
    }
}

@Composable
private fun Choice(
    label: Int,
    missing: Boolean,
    chips: @Composable () -> Unit,
) {
    Text(stringResource(label), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips() }
    if (missing) Text(stringResource(R.string.provider_missing), color = MaterialTheme.colorScheme.error)
}

/** Campo con etiqueta visible y anunciada; `missing` lo marca como faltante (`026` RF-02). */
@Composable
internal fun LabeledField(
    label: Int,
    value: String,
    missing: Boolean,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValue: (String) -> Unit,
) {
    val text = stringResource(label)
    Text(text, style = MaterialTheme.typography.titleSmall)
    VirGoTextField(
        value = value,
        onValueChange = onValue,
        singleLine = singleLine,
        isError = missing,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = text },
    )
    if (missing) Text(stringResource(R.string.provider_missing), color = MaterialTheme.colorScheme.error)
}

private fun noticeText(notice: ProfileNotice): Int =
    when (notice) {
        ProfileNotice.SAVED -> R.string.provider_saved
        ProfileNotice.PUBLISHED -> R.string.provider_published
        ProfileNotice.UNAVAILABLE -> R.string.provider_save_unavailable
        ProfileNotice.FAILED -> R.string.provider_save_failed
    }

@Composable
private fun noticeColor(notice: ProfileNotice) =
    if (notice == ProfileNotice.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

private const val MAX_PRICE_DIGITS = 7
