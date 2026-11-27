package mx.virgo.app.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider

@Composable
fun ClientFavorites(viewModel: FavoritesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    FavoritesContent(state, onRemove = viewModel::remove, onRetry = viewModel::refresh)
}

@Composable
internal fun FavoritesContent(
    state: FavoritesUiState,
    onRemove: (String) -> Unit,
    onRetry: () -> Unit,
) {
    VirGoPageBody {
        when (state.status) {
            FavoritesStatus.LOADING -> LoadingState()
            FavoritesStatus.UNAVAILABLE ->
                Text(stringResource(R.string.favorites_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
            FavoritesStatus.FAILED ->
                ErrorBanner(
                    stringResource(R.string.tools_error),
                    actionLabel = stringResource(R.string.booking_retry),
                    onAction = onRetry,
                )
            FavoritesStatus.READY ->
                if (state.favorites.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.favorites_empty),
                        text = stringResource(R.string.favorites_empty_text),
                        iconVector = Icons.Outlined.FavoriteBorder,
                    )
                } else {
                    FavoriteList(state, onRemove)
                }
        }
        if (state.removeFailed) {
            Text(
                stringResource(R.string.favorites_remove_failed),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun FavoriteList(
    state: FavoritesUiState,
    onRemove: (String) -> Unit,
) {
    VirGoCard {
        Column(Modifier.padding(horizontal = 16.dp)) {
            state.favorites.forEachIndexed { index, favorite ->
                if (index > 0) VirGoRowDivider()
                val name = favorite.name ?: stringResource(R.string.favorites_unnamed)
                VirGoRow(
                    title = name,
                    subtitle = favorite.rating?.let { stringResource(R.string.favorites_rating, it) },
                    standalone = false,
                    trailing = {
                        IconButton(onClick = { onRemove(favorite.targetId) }, enabled = state.removing == null) {
                            Icon(
                                Icons.Filled.Favorite,
                                stringResource(R.string.explore_favorite_remove, name),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            }
        }
    }
}

/** Corazón en el encabezado del detalle; no se dibuja si el backend no tiene favoritos. */
@Composable
fun FavoriteHeart(
    venueId: String,
    name: String,
    viewModel: FavoriteHeartViewModel = hiltViewModel(key = "heart-$venueId"),
) {
    LaunchedEffect(venueId) { viewModel.load(venueId) }
    val state by viewModel.state.collectAsState()
    val favorite = state.favorite ?: return
    IconButton(onClick = { viewModel.toggle(name) }, enabled = !state.busy) {
        Icon(
            if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            stringResource(if (favorite) R.string.explore_favorite_remove else R.string.explore_favorite_add, name),
            tint = if (favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
