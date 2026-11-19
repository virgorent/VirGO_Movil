package mx.virgo.app.ui.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.kmText
import mx.virgo.app.ui.guest.ratingText
import mx.virgo.core.designsystem.components.VirGoBadge
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate700
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleLight
import java.util.Locale

private val SmallGap = 8.dp
private val CardPadding = 12.dp
private val ContentGap = 10.dp
private val InnerGap = 4.dp
private val PhotoSize = 64.dp
private val CardGap = 10.dp

/** C11: recomendaciones según tipo de evento, invitados y presupuesto (RF-21 a RF-23). */
@Composable
internal fun ExploreRecommendedPanel(
    state: ExploreUiState,
    onEventTypeChange: (ExploreEventType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExplorePanel(
        title = stringResource(R.string.explore_recommended_title),
        onClose = onClose,
        modifier = modifier,
    ) {
        val summary = summaryText(state.filters)
        if (summary != null) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
            )
        }
        ExploreEventRow(eventType = state.eventType, onEventTypeChange = onEventTypeChange)
        if (state.recommendations.isEmpty()) {
            Text(
                text = stringResource(R.string.explore_recommended_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(CardGap)) {
                state.recommendations.forEach { recommendation ->
                    ExploreRecommendationCard(
                        recommendation = recommendation,
                        filters = state.filters,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreEventRow(
    eventType: ExploreEventType,
    onEventTypeChange: (ExploreEventType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.explore_event_cd),
            style = MaterialTheme.typography.labelMedium,
            color = Slate700,
        )
        Spacer(modifier = Modifier.height(SmallGap))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SmallGap),
        ) {
            ExploreEvents.forEach { option ->
                ExploreChip(
                    label = option.label(),
                    selected = option == eventType,
                    onClick = { onEventTypeChange(option) },
                )
            }
        }
    }
}

/** RF-22: porcentaje de encaje, motivo e insignia de estimación (RF-23). */
@Composable
private fun ExploreRecommendationCard(
    recommendation: ExploreRecommendation,
    filters: ExploreFilters,
    modifier: Modifier = Modifier,
) {
    val venue = recommendation.venue
    VirGoCard(
        containerColor = VirGoPurpleLight,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(CardPadding),
            horizontalArrangement = Arrangement.spacedBy(ContentGap),
        ) {
            ExploreVenuePhoto(size = PhotoSize)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = venue.name,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(InnerGap))
                    VirGoBadge(
                        text = stringResource(R.string.explore_recommended_fit, recommendation.fitPercent),
                        containerColor = VirGoPurple,
                        contentColor = Color.White,
                    )
                }
                Text(
                    text = venue.metaText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                )
                Spacer(modifier = Modifier.height(InnerGap))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reasonText(recommendation = recommendation, filters = filters),
                        style = MaterialTheme.typography.labelMedium,
                        color = VirGoPurple,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    VirGoBadge(
                        text = stringResource(R.string.explore_ai_badge),
                        containerColor = Color.White,
                        contentColor = VirGoPurple,
                    )
                }
            }
        }
    }
}

@Composable
private fun summaryText(filters: ExploreFilters): String? {
    val guests = filters.guestsCount
    val budget = filters.budgetAmount
    val guestsText = guests?.let { stringResource(R.string.explore_summary_guests, it) }
    val budgetText =
        budget?.let {
            stringResource(R.string.explore_summary_budget, moneyText(it))
        }
    return when {
        guestsText != null && budgetText != null ->
            stringResource(R.string.explore_recommended_summary, guestsText, budgetText)
        guestsText != null -> guestsText
        else -> budgetText
    }
}

@Composable
private fun reasonText(
    recommendation: ExploreRecommendation,
    filters: ExploreFilters,
): String =
    when (recommendation.reason) {
        ExploreReason.CAPACITY ->
            stringResource(R.string.explore_reason_capacity, filters.guestsCount ?: 0)
        ExploreReason.BUDGET -> stringResource(R.string.explore_reason_budget)
        ExploreReason.CLOSE ->
            stringResource(R.string.explore_reason_close, recommendation.venue.kmText())
        ExploreReason.RATING ->
            stringResource(R.string.explore_reason_rating, recommendation.venue.ratingText())
    }

private fun moneyText(amount: Double): String = String.format(Locale.ROOT, "%,.0f", amount)
