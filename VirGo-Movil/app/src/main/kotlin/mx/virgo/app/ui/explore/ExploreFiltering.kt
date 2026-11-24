package mx.virgo.app.ui.explore

import mx.virgo.app.ui.guest.CATEGORY_ELEGANT
import mx.virgo.app.ui.guest.CATEGORY_GARDENS
import mx.virgo.app.ui.guest.CATEGORY_HACIENDAS
import mx.virgo.app.ui.guest.CATEGORY_KIDS
import mx.virgo.app.ui.guest.CATEGORY_POOL
import mx.virgo.app.ui.guest.VenueSample
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val DateFormatter = DateTimeFormatter.ofPattern(EXPLORE_DATE_PATTERN)

private const val BASE_POINTS = 40
private const val CAPACITY_BONUS = 25
private const val CAPACITY_PENALTY = -30
private const val BUDGET_BONUS = 20
private const val BUDGET_PENALTY = -25
private const val CLOSE_BONUS = 5
private const val EVENT_BONUS = 10
private const val CLOSE_KM = 3.0
private const val RATING_POINTS_PER_STAR = 2.0
private const val RATING_WEIGHT = 10.0
private const val DISTANCE_WEIGHT = 1.0
private const val FIT_MIN = 0
private const val FIT_MAX = 100

private data class ExploreFit(
    val percent: Int,
    val reason: ExploreReason,
)

/** RF-18: solo los salones que cumplen todos los filtros activos. */
internal fun applyExploreFilters(
    catalog: List<VenueSample>,
    filters: ExploreFilters,
): List<VenueSample> = catalog.filter { filters.matches(it) }

private fun ExploreFilters.matches(venue: VenueSample): Boolean {
    val queryOk =
        query.isBlank() ||
            venue.name.contains(query, ignoreCase = true) ||
            venue.area.contains(query, ignoreCase = true)
    val categoryOk = category == null || category in venue.categories
    val distanceOk = venue.distanceKm <= radiusKm
    val guestsOk = guestsCount?.let { venue.capacity >= it } ?: true
    val budgetOk = budgetAmount?.let { venue.priceBaseAmount?.let { price -> price <= it } ?: true } ?: true
    return queryOk && categoryOk && distanceOk && guestsOk && budgetOk
}

internal fun sortExploreResults(
    results: List<VenueSample>,
    order: ExploreOrder,
): List<VenueSample> =
    when (order) {
        ExploreOrder.RECOMMENDED ->
            results.sortedWith(
                compareByDescending<VenueSample> { it.compositeScore() }.thenBy { it.distanceKm },
            )
        ExploreOrder.NEARNESS -> results.sortedBy { it.distanceKm }
        ExploreOrder.PRICE ->
            results.sortedWith(
                compareBy<VenueSample> { it.priceBaseAmount ?: Double.MAX_VALUE }.thenBy { it.name },
            )
        ExploreOrder.RATING ->
            results.sortedWith(
                compareByDescending<VenueSample> { it.rating }.thenBy { it.distanceKm },
            )
    }

/** Orden «Recomendado»: mezcla calificación y cercanía (RF-11). */
private fun VenueSample.compositeScore(): Double = rating * RATING_WEIGHT - distanceKm * DISTANCE_WEIGHT

/**
 * RF-21 y RF-22: qué tan bien encaja cada salón con el tipo de evento, los invitados
 * y el presupuesto. La fecha (RF-16) se valida y se guarda, pero todavía no descarta
 * salones: `007-venues-catalog` no entrega disponibilidad en su contrato.
 */
internal fun buildExploreRecommendations(
    results: List<VenueSample>,
    filters: ExploreFilters,
    eventType: ExploreEventType,
): List<ExploreRecommendation> =
    results
        .map { venue -> venue to fitOf(venue, filters, eventType) }
        .sortedByDescending { it.second.percent }
        .take(EXPLORE_RECOMMENDATION_LIMIT)
        .map { (venue, fit) -> ExploreRecommendation(venue, fit.percent, fit.reason) }

private fun fitOf(
    venue: VenueSample,
    filters: ExploreFilters,
    eventType: ExploreEventType,
): ExploreFit {
    val guests = filters.guestsCount
    val budget = filters.budgetAmount
    var points = BASE_POINTS
    var reason = ExploreReason.RATING
    if (guests != null) {
        if (venue.capacity >= guests) {
            points += CAPACITY_BONUS
            reason = ExploreReason.CAPACITY
        } else {
            points += CAPACITY_PENALTY
        }
    }
    if (budget != null) {
        val price = venue.priceBaseAmount
        if (price == null || price <= budget) {
            points += BUDGET_BONUS
            if (reason == ExploreReason.RATING) {
                reason = ExploreReason.BUDGET
            }
        } else {
            points += BUDGET_PENALTY
        }
    }
    if (venue.distanceKm <= CLOSE_KM) {
        points += CLOSE_BONUS
        if (reason == ExploreReason.RATING) {
            reason = ExploreReason.CLOSE
        }
    }
    if (preferredCategories(eventType).any { it in venue.categories }) {
        points += EVENT_BONUS
    }
    points += (venue.rating * RATING_POINTS_PER_STAR).roundToInt()
    return ExploreFit(percent = points.coerceIn(FIT_MIN, FIT_MAX), reason = reason)
}

private fun preferredCategories(eventType: ExploreEventType): List<String> =
    when (eventType) {
        ExploreEventType.WEDDING -> listOf(CATEGORY_HACIENDAS, CATEGORY_GARDENS)
        ExploreEventType.BIRTHDAY -> listOf(CATEGORY_KIDS, CATEGORY_POOL)
        ExploreEventType.CORPORATE -> listOf(CATEGORY_ELEGANT)
        ExploreEventType.FAMILY -> listOf(CATEGORY_GARDENS, CATEGORY_POOL)
    }

/** RF-18: el panel de filtros no se cierra con valores inválidos (RF-16 valida la fecha). */
internal fun validateExploreFilters(filters: ExploreFilters): ExploreFieldError =
    when {
        filters.guests.isNotBlank() && filters.guestsCount == null -> ExploreFieldError.GUESTS
        filters.budget.isNotBlank() && filters.budgetAmount == null -> ExploreFieldError.BUDGET
        filters.date.isNotBlank() && !isValidDate(filters.date) -> ExploreFieldError.DATE
        else -> ExploreFieldError.NONE
    }

private fun isValidDate(value: String): Boolean = runCatching { LocalDate.parse(value.trim(), DateFormatter) }.isSuccess
