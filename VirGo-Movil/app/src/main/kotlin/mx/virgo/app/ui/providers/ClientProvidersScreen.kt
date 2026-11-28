package mx.virgo.app.ui.providers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.app.ui.quotes.QuoteRequestActions
import mx.virgo.app.ui.quotes.QuoteRequestScreen
import mx.virgo.app.ui.quotes.QuoteTarget
import mx.virgo.app.ui.venuedetail.formatMxn
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoCoverPhoto
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoPageHeader
import mx.virgo.core.designsystem.components.VirGoPhotoPager
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.Provider
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.domain.VenueReviews

/** «Proveedores» del cliente (`011`): catálogo → comparar o detalle → cotizar. */
@Composable
fun ClientProviders(
    onBack: (() -> Unit)?,
    onMyQuotes: () -> Unit,
    onMessage: () -> Unit,
    viewModel: ProvidersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var quoting by rememberSaveable { mutableStateOf(false) }
    val id = openId
    when {
        id != null && quoting -> {
            BackHandler { quoting = false }
            val name =
                state.providers
                    .firstOrNull { it.id == id }
                    ?.businessName
                    .orEmpty()
            QuoteRequestScreen(
                QuoteTarget.provider(id, name),
                QuoteRequestActions(onBack = { quoting = false }, onMyQuotes = onMyQuotes),
            )
        }
        id != null -> {
            BackHandler { openId = null }
            ProviderDetailRoute(id, onBack = { openId = null }, onQuote = { quoting = true }, onMessage = onMessage)
        }
        state.comparing -> {
            BackHandler { viewModel.showCompare(false) }
            Column(Modifier.fillMaxSize()) {
                ProvidersHeader(R.string.providers_compare_title) { viewModel.showCompare(false) }
                VirGoPageBody { CompareTable(state.compared) }
            }
        }
        else ->
            Column(Modifier.fillMaxSize()) {
                ProvidersHeader(R.string.demo_title_providers, onBack)
                ProvidersCatalogContent(
                    state,
                    ProvidersCatalogActions(
                        onCategory = viewModel::selectCategory,
                        onQuery = viewModel::search,
                        onCompareToggle = viewModel::toggleCompare,
                        onCompare = { viewModel.showCompare(true) },
                        onOpen = { openId = it },
                        onRetry = viewModel::refresh,
                    ),
                )
            }
    }
}

@Composable
private fun ProvidersHeader(
    title: Int,
    onBack: (() -> Unit)?,
) = VirGoPageHeader(
    title = stringResource(title),
    kicker = stringResource(R.string.demo_role_client),
    onBack = onBack,
    backContentDescription = stringResource(R.string.demo_back),
)

class ProvidersCatalogActions(
    val onCategory: (ProviderCategory?) -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onCompareToggle: (String) -> Unit = {},
    val onCompare: () -> Unit = {},
    val onOpen: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
internal fun ProvidersCatalogContent(
    state: ProvidersUiState,
    actions: ProvidersCatalogActions,
) {
    VirGoPageBody {
        when (state.load) {
            Load.LOADING -> LoadingState()
            Load.UNAVAILABLE -> ComingSoon(R.string.providers_unavailable)
            Load.FAILED ->
                ErrorBanner(
                    stringResource(R.string.providers_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = actions.onRetry,
                )
            Load.READY -> {
                CategoryChips(state.category, actions.onCategory)
                VirGoTextField(
                    value = state.query,
                    onValueChange = actions.onQuery,
                    placeholder = stringResource(R.string.providers_search),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.compare.size >= MIN_COMPARE) {
                    VirGoButton(
                        onClick = actions.onCompare,
                        text = stringResource(R.string.providers_compare_open, state.compare.size),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state.visible.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.providers_empty),
                        iconVector = Icons.Outlined.Handshake,
                    )
                }
                state.visible.forEach { provider ->
                    ProviderCard(provider, provider.id in state.compare, state.compare.size >= MAX_COMPARE, actions)
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selected: ProviderCategory?,
    onCategory: (ProviderCategory?) -> Unit,
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null,
            onClick = { onCategory(null) },
            label = { Text(stringResource(R.string.providers_all)) },
        )
        ProviderCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onCategory(category) },
                label = { Text(stringResource(categoryLabel(category))) },
            )
        }
    }
}

@Composable
private fun ProviderCard(
    provider: Provider,
    marked: Boolean,
    full: Boolean,
    actions: ProvidersCatalogActions,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            VirGoCoverPhoto(provider.coverUrl ?: provider.gallery.firstOrNull(), contentDescription = null)
            VirGoRow(
                title = provider.businessName,
                subtitle = providerSummary(provider),
                standalone = false,
                onClick = { actions.onOpen(provider.id) },
                subtitleMaxLines = Int.MAX_VALUE,
            )
            FilterChip(
                selected = marked,
                enabled = marked || !full,
                onClick = { actions.onCompareToggle(provider.id) },
                label = {
                    Text(stringResource(if (marked) R.string.providers_compare_marked else R.string.providers_compare))
                },
            )
        }
    }
}

@Composable
private fun providerSummary(provider: Provider): String =
    listOfNotNull(
        provider.category?.let { stringResource(categoryLabel(it)) },
        provider.priceFrom?.let { stringResource(R.string.providers_price_from, formatMxn(it.toDouble())) },
        if (provider.ratingCount > 0) stringResource(R.string.providers_rating, provider.ratingAverage) else null,
        provider.zones.takeIf { it.isNotEmpty() }?.joinToString(", "),
    ).joinToString(" · ")

/** Tabla lado a lado (`011` RF-02); resalta la mejor calificación. */
@Composable
internal fun CompareTable(providers: List<Provider>) {
    val perPerson = pricingText(ProviderPricing.PER_PERSON)
    val perEvent = pricingText(ProviderPricing.PER_EVENT)
    val best = providers.filter { it.ratingCount > 0 }.maxByOrNull { it.ratingAverage }?.id
    val rows: List<Pair<Int, (Provider) -> String>> =
        listOf(
            R.string.providers_row_price to { p -> p.priceFrom?.let { formatMxn(it.toDouble()) } ?: "—" },
            R.string.providers_row_pricing to { p ->
                when (p.pricing) {
                    ProviderPricing.PER_PERSON -> perPerson
                    ProviderPricing.PER_EVENT -> perEvent
                    null -> "—"
                }
            },
            R.string.providers_row_packages to { p -> p.packages.size.toString() },
            R.string.providers_row_rating to { p ->
                if (p.ratingCount > 0) "%.1f (%d)".format(p.ratingAverage, p.ratingCount) else "—"
            },
            R.string.providers_row_response to { p -> p.responseTime.ifBlank { "—" } },
            R.string.providers_row_zones to { p -> p.zones.joinToString(", ").ifBlank { "—" } },
        )
    val labels = rows.map { (label, _) -> stringResource(label) }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        providers.forEach { provider ->
            VirGoCard(Modifier.width(COMPARE_COLUMN_DP.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        provider.businessName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (provider.id == best) {
                        Text(
                            stringResource(R.string.providers_best),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    rows.forEachIndexed { index, (_, value) ->
                        Text(labels[index], style = MaterialTheme.typography.labelSmall)
                        Text(value(provider), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderDetailRoute(
    id: String,
    onBack: () -> Unit,
    onQuote: () -> Unit,
    onMessage: () -> Unit,
    viewModel: ProviderDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(id) { viewModel.open(id) }
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        VirGoPageHeader(
            title = state.provider?.businessName ?: stringResource(R.string.demo_title_provider),
            kicker = stringResource(R.string.demo_role_client),
            onBack = onBack,
            backContentDescription = stringResource(R.string.demo_back),
        )
        VirGoPageBody {
            val provider = state.provider
            when {
                provider != null -> ProviderDetailBody(provider, state.reviews, onQuote, onMessage)
                state.load == Load.UNAVAILABLE -> ComingSoon(R.string.providers_unavailable)
                state.load == Load.FAILED ->
                    ErrorBanner(
                        stringResource(R.string.providers_error),
                        actionLabel = stringResource(R.string.booking_retry),
                        onAction = viewModel::retry,
                    )
                else -> LoadingState()
            }
        }
    }
}

@Composable
internal fun ProviderDetailBody(
    provider: Provider,
    reviews: VenueReviews?,
    onQuote: () -> Unit,
    onMessage: () -> Unit,
) {
    VirGoPhotoPager(
        listOfNotNull(provider.coverUrl) + provider.gallery,
        pageLabel = { page, total -> stringResource(R.string.venue_photo_page, page, total) },
        photoDescription = { page, total ->
            stringResource(R.string.venue_photo_description, page, total, provider.businessName)
        },
    )
    Text(provider.description, style = MaterialTheme.typography.bodyLarge)
    if (provider.zones.isNotEmpty()) {
        Text(stringResource(R.string.providers_zones, provider.zones.joinToString(", ")))
    }
    if (provider.responseTime.isNotBlank()) {
        Text(stringResource(R.string.providers_response, provider.responseTime))
    }
    VirGoSection(title = stringResource(R.string.providers_packages)) {
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                provider.packages.forEachIndexed { index, item ->
                    if (index > 0) VirGoRowDivider()
                    VirGoRow(
                        title = item.name,
                        subtitle =
                            listOfNotNull(
                                item.price?.let { formatMxn(it.toDouble()) },
                                item.includes.takeIf { it.isNotBlank() },
                            ).joinToString(" · "),
                        standalone = false,
                        subtitleMaxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
    }
    VirGoButton(onClick = onQuote, text = stringResource(R.string.providers_quote), modifier = Modifier.fillMaxWidth())
    VirGoButton(
        onClick = onMessage,
        text = stringResource(R.string.providers_message),
        modifier = Modifier.fillMaxWidth(),
    )
    ReviewsSection(reviews)
}

private const val COMPARE_COLUMN_DP = 200
