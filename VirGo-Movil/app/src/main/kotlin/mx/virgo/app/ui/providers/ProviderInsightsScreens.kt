package mx.virgo.app.ui.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.R
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.ui.ownerrequests.AnswerForm
import mx.virgo.app.ui.ownerrequests.OwnerRequests
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.ProviderMonthStats
import mx.virgo.core.domain.ProviderPackage
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteConcept
import mx.virgo.core.domain.QuoteConcepts
import mx.virgo.core.domain.QuotesRepository
import mx.virgo.core.domain.VenueReviews
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ProviderInsightsUiState(
    val load: Load = Load.LOADING,
    val packages: List<ProviderPackage> = emptyList(),
    val reviews: VenueReviews? = null,
    val reviewsLoad: Load = Load.LOADING,
    val stats: List<ProviderMonthStats> = emptyList(),
    val statsLoad: Load = Load.LOADING,
    val months: Int = THREE_MONTHS,
)

const val THREE_MONTHS = 3
const val SIX_MONTHS = 6

/** Paquetes (para responder), reseñas y estadísticas del proveedor (`028` RF-02, `029`). */
@HiltViewModel
class ProviderInsightsViewModel
    @Inject
    constructor(
        private val providers: ProvidersRepository,
        private val quotes: QuotesRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProviderInsightsUiState())
        val state: StateFlow<ProviderInsightsUiState> = ui.asStateFlow()

        /** Reloj inyectable en pruebas. */
        internal var today: () -> LocalDate = { LocalDate.now() }
        private var allQuotes = emptyList<Quote>()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ProviderInsightsUiState(months = ui.value.months)
            viewModelScope.launch {
                loadProfile()
                when (val result = quotes.list()) {
                    is AppResult.Success -> {
                        allQuotes = result.value
                        ui.value = ui.value.copy(statsLoad = Load.READY, stats = stats(ui.value.months))
                    }
                    is AppResult.Failure ->
                        session.loadFor(result.error)?.let { ui.value = ui.value.copy(statsLoad = it) }
                }
            }
        }

        fun selectMonths(months: Int) {
            ui.value = ui.value.copy(months = months, stats = stats(months))
        }

        private suspend fun loadProfile() {
            when (val mine = providers.mine()) {
                is AppResult.Failure ->
                    session.loadFor(mine.error)?.let { ui.value = ui.value.copy(load = it, reviewsLoad = it) }
                is AppResult.Success -> {
                    val provider = mine.value
                    ui.value = ui.value.copy(load = Load.READY, packages = provider?.packages.orEmpty())
                    if (provider ==
                        null
                    ) {
                        ui.value = ui.value.copy(reviewsLoad = Load.READY)
                    } else {
                        loadReviews(provider.id)
                    }
                }
            }
        }

        private suspend fun loadReviews(providerId: String) {
            when (val reviews = providers.reviews(providerId)) {
                is AppResult.Success -> ui.value = ui.value.copy(reviewsLoad = Load.READY, reviews = reviews.value)
                is AppResult.Failure ->
                    session.loadFor(reviews.error)?.let { ui.value = ui.value.copy(reviewsLoad = it) }
            }
        }

        private fun stats(months: Int) = ProviderMonthStats.from(allQuotes, months, today())
    }

/** Solicitudes del proveedor (`028`): las del propietario (`024`) más los conceptos de sus paquetes. */
@Composable
fun ProviderRequests(viewModel: ProviderInsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    OwnerRequests(answerHelper = { form, onForm -> ConceptsCard(state.packages, form, onForm) })
}

/** Conceptos precargados con los paquetes; «Usar este total» llena el total y el desglose (`028` RF-02, RF-03). */
@Composable
internal fun ConceptsCard(
    packages: List<ProviderPackage>,
    form: AnswerForm,
    onForm: (AnswerForm) -> Unit,
) {
    var concepts by remember(packages) { mutableStateOf(QuoteConcepts.fromPackages(packages)) }
    VirGoSection(title = stringResource(R.string.provider_concepts)) {
        concepts.forEachIndexed { index, concept ->
            ConceptRow(
                concept,
                onChange = { changed -> concepts = concepts.mapIndexed { i, old -> if (i == index) changed else old } },
                onRemove = { concepts = concepts.filterIndexed { i, _ -> i != index } },
            )
        }
        TextButton(onClick = { concepts = concepts + QuoteConcept() }) {
            Text(stringResource(R.string.provider_concept_add))
        }
        val total = QuoteConcepts.total(concepts)
        Text(stringResource(R.string.provider_concepts_total, formatMxn(total.toDouble())))
        VirGoButton(
            onClick = {
                val summary = QuoteConcepts.summary(concepts)
                val message = if (form.message.isBlank()) summary else form.message
                onForm(form.copy(total = total.toPlainString(), message = message))
            },
            enabled = total.signum() > 0,
            text = stringResource(R.string.provider_concepts_use),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ConceptRow(
    concept: QuoteConcept,
    onChange: (QuoteConcept) -> Unit,
    onRemove: () -> Unit,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LabeledField(R.string.provider_concept_name, concept.name, false) { onChange(concept.copy(name = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallNumber(R.string.provider_concept_quantity, concept.quantity.toString(), Modifier.weight(1f)) {
                    onChange(concept.copy(quantity = it.toIntOrNull() ?: 0))
                }
                SmallNumber(R.string.provider_concept_price, concept.unitPrice.toPlainString(), Modifier.weight(1f)) {
                    onChange(concept.copy(unitPrice = it.toBigDecimalOrNull() ?: BigDecimal.ZERO))
                }
            }
            TextButton(onClick = onRemove) { Text(stringResource(R.string.provider_package_remove)) }
        }
    }
}

@Composable
private fun SmallNumber(
    label: Int,
    value: String,
    modifier: Modifier,
    onValue: (String) -> Unit,
) {
    val text = stringResource(label)
    Column(modifier) {
        Text(text, style = MaterialTheme.typography.labelMedium)
        VirGoTextField(
            value = value,
            onValueChange = { typed -> onValue(typed.filter(Char::isDigit).take(MAX_NUMBER_DIGITS)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = text },
        )
    }
}

@Composable
fun ProviderReviews(viewModel: ProviderInsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    VirGoPageBody {
        when (state.reviewsLoad) {
            Load.LOADING -> LoadingState()
            Load.UNAVAILABLE -> ComingSoon(R.string.provider_reviews_unavailable)
            Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = viewModel::refresh,
                )
            Load.READY -> ReviewsSection(state.reviews)
        }
    }
}

@Composable
fun ProviderStats(viewModel: ProviderInsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    ProviderStatsContent(state, viewModel::selectMonths, viewModel::refresh)
}

@Composable
internal fun ProviderStatsContent(
    state: ProviderInsightsUiState,
    onMonths: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    VirGoPageBody {
        when (state.statsLoad) {
            Load.LOADING -> LoadingState()
            Load.UNAVAILABLE -> ComingSoon(R.string.provider_stats_unavailable)
            Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = onRetry,
                )
            Load.READY -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(THREE_MONTHS, SIX_MONTHS).forEach { months ->
                        FilterChip(
                            selected = state.months == months,
                            onClick = { onMonths(months) },
                            label = { Text(stringResource(R.string.provider_stats_months, months)) },
                        )
                    }
                }
                VirGoCard {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        state.stats.forEachIndexed { index, month ->
                            if (index > 0) VirGoRowDivider()
                            VirGoRow(
                                title = month.month.format(MONTH_FORMAT).replaceFirstChar { it.titlecase(SPANISH) },
                                subtitle =
                                    stringResource(
                                        R.string.provider_stats_row,
                                        month.received,
                                        month.accepted,
                                        month.conversion,
                                    ),
                                standalone = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MAX_NUMBER_DIGITS = 7
private val SPANISH = Locale.forLanguageTag("es-MX")
private val MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", SPANISH)
