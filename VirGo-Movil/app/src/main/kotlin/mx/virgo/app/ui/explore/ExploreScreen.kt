package mx.virgo.app.ui.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import mx.virgo.app.LocalDevMenu
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.guest.ExploreCategories
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.Slate800
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple

private val ScreenHPadding = 16.dp
private val ScreenVPadding = 10.dp
private val RowGap = 8.dp
private val SearchShape = RoundedCornerShape(16.dp)
private val BadgeSize = 18.dp
private val BadgeOffset = 6.dp
private val SearchPadding = 14.dp

/**
 * Pantalla principal del Cliente (spec `005`): B8 con mapa o lista (C9) y las hojas
 * de filtros (C10) y recomendaciones (C11).
 */
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val favoriteError = stringResource(R.string.explore_favorite_error)
    val noticesSoon = stringResource(R.string.explore_notices_soon)
    val providersSoon = stringResource(R.string.explore_providers_soon)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ExploreUiEvent.FavoriteError -> snackbarHost.showSnackbar(favoriteError)
            }
        }
    }
    BackHandler(enabled = state.sheet != ExploreSheet.NONE) {
        viewModel.onSheetChange(ExploreSheet.NONE)
    }

    Box(modifier = modifier.fillMaxSize()) {
        ExploreContent(
            state = state,
            viewModel = viewModel,
            onNoticesClick = { scope.launch { snackbarHost.showSnackbar(noticesSoon) } },
            onProvidersClick = { scope.launch { snackbarHost.showSnackbar(providersSoon) } },
        )

        ExploreSheetHost(
            state = state,
            onFiltersChange = viewModel::onFiltersChange,
            onApply = viewModel::onFiltersApply,
            onClear = viewModel::onFiltersClear,
            onEventTypeChange = viewModel::onEventTypeChange,
            onClose = { viewModel.onSheetChange(ExploreSheet.NONE) },
        )
    }
}

/** Cabecera (marca, avisos, buscador, categorías y modo) más el contenido mapa/lista. */
@Composable
private fun ExploreContent(
    state: ExploreUiState,
    viewModel: ExploreViewModel,
    onNoticesClick: () -> Unit,
    onProvidersClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ExploreHeader(
            state = state,
            onFiltersClick = { viewModel.onSheetChange(ExploreSheet.FILTERS) },
            onRecommendedClick = { viewModel.onSheetChange(ExploreSheet.RECOMMENDED) },
            onNoticesClick = onNoticesClick,
            onProvidersClick = onProvidersClick,
            onCategoryChange = { category ->
                viewModel.onFiltersChange { copy(category = category) }
            },
            onModeChange = viewModel::onModeChange,
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state.mode) {
                ExploreMode.MAP ->
                    ExploreMapContent(
                        state = state,
                        onVenueClick = { id -> viewModel.onVenueSelect(id) },
                        onClearSelection = { viewModel.onVenueSelect(null) },
                        onFavoriteToggle = viewModel::onFavoriteToggle,
                        onRetry = viewModel::refresh,
                    )
                ExploreMode.LIST ->
                    ExploreListContent(
                        state = state,
                        onOrderChange = { order ->
                            viewModel.onFiltersChange { copy(order = order) }
                        },
                        onVenueClick = { id ->
                            viewModel.onVenueSelect(id)
                            viewModel.onModeChange(ExploreMode.MAP)
                        },
                        onFavoriteToggle = viewModel::onFavoriteToggle,
                        onRetry = viewModel::refresh,
                    )
            }
        }
    }
}

@Composable
private fun ExploreSheetHost(
    state: ExploreUiState,
    onFiltersChange: (ExploreFilters.() -> ExploreFilters) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onEventTypeChange: (ExploreEventType) -> Unit,
    onClose: () -> Unit,
) {
    when (state.sheet) {
        ExploreSheet.FILTERS ->
            ExploreFiltersPanel(
                state = state,
                onFiltersChange = onFiltersChange,
                onApply = onApply,
                onClear = onClear,
                onClose = onClose,
            )
        ExploreSheet.RECOMMENDED ->
            ExploreRecommendedPanel(
                state = state,
                onEventTypeChange = onEventTypeChange,
                onClose = onClose,
            )
        ExploreSheet.NONE -> Unit
    }
}

@Composable
private fun ExploreHeader(
    state: ExploreUiState,
    onFiltersClick: () -> Unit,
    onRecommendedClick: () -> Unit,
    onNoticesClick: () -> Unit,
    onProvidersClick: () -> Unit,
    onCategoryChange: (String?) -> Unit,
    onModeChange: (ExploreMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ScreenHPadding,
                    vertical = ScreenVPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        ExploreTopRow(
            unreadNotices = state.unreadNotices,
            onNoticesClick = onNoticesClick,
        )
        ExploreSearchBar(query = state.filters.query, onClick = onFiltersClick)
        ExploreCategoryRow(
            selected = state.filters.category,
            onCategoryChange = onCategoryChange,
        )
        ExploreActionsRow(
            mode = state.mode,
            onModeChange = onModeChange,
            onProvidersClick = onProvidersClick,
            onRecommendedClick = onRecommendedClick,
        )
    }
}

/** Marca, campana con los avisos sin leer (RF-07) y botón «Para ti» (RF-05). */
@Composable
private fun ExploreTopRow(
    unreadNotices: Int,
    onNoticesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.explore_home_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 2,
            )
            Text(
                text = stringResource(R.string.explore_home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
            )
        }
        ExploreNoticesButton(unreadNotices = unreadNotices, onClick = onNoticesClick)
        LocalDevMenu.current()
    }
}

/** RF-07: campana de avisos con el contador de no leídos. */
@Composable
private fun ExploreNoticesButton(
    unreadNotices: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = stringResource(R.string.explore_notices_cd, unreadNotices),
                tint = Slate700,
            )
        }
        if (unreadNotices > 0) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(BadgeOffset)
                        .size(BadgeSize)
                        .background(ErrorRed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unreadNotices.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

/** RF-03: el buscador abre los filtros. */
@Composable
private fun ExploreSearchBar(
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(SearchShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = SearchPadding,
                    vertical = SearchPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Slate400,
        )
        Spacer(modifier = Modifier.width(RowGap))
        Text(
            text = query.ifBlank { stringResource(R.string.explore_search_hint) },
            style = MaterialTheme.typography.bodyMedium,
            color = if (query.isBlank()) Slate400 else Slate800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = stringResource(R.string.explore_filters_cd),
            tint = VirGoPurple,
        )
    }
}

/** RF-04: categorías de la maqueta. */
@Composable
private fun ExploreCategoryRow(
    selected: String?,
    onCategoryChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        ExploreChip(
            label = stringResource(R.string.explore_category_all),
            selected = selected == null,
            onClick = { onCategoryChange(null) },
        )
        ExploreCategories.forEach { category ->
            ExploreChip(
                label = categoryLabel(category),
                selected = selected == category,
                onClick = { onCategoryChange(category) },
            )
        }
    }
}

/** Acceso a Proveedores (RF-06) y cambio entre mapa y lista (RF-09). */
@Composable
private fun ExploreActionsRow(
    mode: ExploreMode,
    onModeChange: (ExploreMode) -> Unit,
    onProvidersClick: () -> Unit,
    onRecommendedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onProvidersClick) {
            Text(
                text = stringResource(R.string.explore_providers),
                style = MaterialTheme.typography.labelLarge,
                color = VirGoPurple,
            )
        }
        TextButton(onClick = onRecommendedClick) {
            Text(
                text = stringResource(R.string.explore_for_you),
                style = MaterialTheme.typography.labelLarge,
                color = VirGoPurple,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
            ExploreChip(
                label = stringResource(R.string.explore_mode_map),
                selected = mode == ExploreMode.MAP,
                onClick = { onModeChange(ExploreMode.MAP) },
            )
            ExploreChip(
                label = stringResource(R.string.explore_mode_list),
                selected = mode == ExploreMode.LIST,
                onClick = { onModeChange(ExploreMode.LIST) },
            )
        }
    }
}
