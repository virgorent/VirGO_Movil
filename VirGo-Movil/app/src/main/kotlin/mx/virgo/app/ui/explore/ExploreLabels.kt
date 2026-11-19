package mx.virgo.app.ui.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R
import mx.virgo.app.ui.guest.CATEGORY_ELEGANT
import mx.virgo.app.ui.guest.CATEGORY_GARDENS
import mx.virgo.app.ui.guest.CATEGORY_HACIENDAS
import mx.virgo.app.ui.guest.CATEGORY_KIDS
import mx.virgo.app.ui.guest.CATEGORY_POOL

/** Etiqueta de una categoría del catálogo (RF-04). */
@Composable
internal fun categoryLabel(category: String): String =
    when (category) {
        CATEGORY_GARDENS -> stringResource(R.string.explore_category_gardens)
        CATEGORY_POOL -> stringResource(R.string.explore_category_pool)
        CATEGORY_ELEGANT -> stringResource(R.string.explore_category_elegant)
        CATEGORY_HACIENDAS -> stringResource(R.string.explore_category_haciendas)
        CATEGORY_KIDS -> stringResource(R.string.explore_category_kids)
        else -> category
    }

/** Etiqueta de un orden de resultados (RF-11 y RF-17). */
@Composable
internal fun ExploreOrder.label(): String =
    when (this) {
        ExploreOrder.RECOMMENDED -> stringResource(R.string.explore_sort_recommended)
        ExploreOrder.NEARNESS -> stringResource(R.string.explore_sort_nearness)
        ExploreOrder.PRICE -> stringResource(R.string.explore_sort_price)
        ExploreOrder.RATING -> stringResource(R.string.explore_sort_rating)
    }

/** Etiqueta de un tipo de evento de las recomendaciones (RF-21). */
@Composable
internal fun ExploreEventType.label(): String =
    when (this) {
        ExploreEventType.WEDDING -> stringResource(R.string.explore_event_wedding)
        ExploreEventType.BIRTHDAY -> stringResource(R.string.explore_event_birthday)
        ExploreEventType.CORPORATE -> stringResource(R.string.explore_event_corporate)
        ExploreEventType.FAMILY -> stringResource(R.string.explore_event_family)
    }
