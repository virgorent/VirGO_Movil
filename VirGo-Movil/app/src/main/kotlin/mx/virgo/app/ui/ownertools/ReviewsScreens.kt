package mx.virgo.app.ui.ownertools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.LoadingState
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoPageBody
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.domain.Review
import mx.virgo.core.domain.ReviewRules

@Composable
fun OwnerReviews(viewModel: OwnerReviewsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    OwnerReviewsContent(state, OwnerReviewsActions(viewModel::select, viewModel::editReply, viewModel::sendReply))
}

@Composable
internal fun OwnerReviewsContent(
    state: OwnerReviewsUiState,
    actions: OwnerReviewsActions,
) {
    VirGoPageBody {
        VenuePicker(state.venues.map { it.id to it.draft.name }, state.venueId, actions.onVenue)
        val reviews = state.reviews
        when {
            state.status == ReviewsStatus.LOADING -> LoadingState()
            state.status == ReviewsStatus.UNAVAILABLE ->
                Text(stringResource(R.string.reviews_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.status == ReviewsStatus.FAILED -> ErrorBanner(stringResource(R.string.tools_error))
            reviews == null || reviews.reviews.isEmpty() ->
                Text(stringResource(R.string.reviews_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> {
                Text(
                    stringResource(R.string.reviews_summary, reviews.rating, reviews.count),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                reviews.reviews.forEach { ReviewCard(it, state, actions) }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    state: OwnerReviewsUiState,
    actions: OwnerReviewsActions,
) {
    VirGoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(review.authorName, fontWeight = FontWeight.SemiBold)
            StarsRow(review.stars)
            Text(review.text)
            review.reply?.let {
                Text(
                    stringResource(R.string.reviews_your_reply, it.text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val replying = state.replying?.takeIf { it.first == review.id }
            if (replying == null) {
                TextButton(onClick = { actions.onReply(review.id to review.reply?.text.orEmpty()) }) {
                    Text(
                        stringResource(
                            if (review.reply ==
                                null
                            ) {
                                R.string.reviews_reply
                            } else {
                                R.string.reviews_edit_reply
                            },
                        ),
                    )
                }
            } else {
                ReplyForm(replying.second, state, actions)
            }
        }
    }
}

@Composable
private fun ReplyForm(
    text: String,
    state: OwnerReviewsUiState,
    actions: OwnerReviewsActions,
) {
    val label = stringResource(R.string.reviews_reply)
    VirGoTextField(
        value = text,
        onValueChange = { actions.onReply(state.replying?.first.orEmpty() to it) },
        placeholder = label,
        singleLine = false,
        modifier = Modifier.semantics { contentDescription = label },
    )
    if (state.replyFailed) Text(stringResource(R.string.reviews_reply_failed), color = MaterialTheme.colorScheme.error)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VirGoButton(
            onClick = actions.onSend,
            enabled = !state.sending && text.isNotBlank(),
            text = stringResource(R.string.reviews_send),
        )
        VirGoOutlinedButton(onClick = { actions.onReply(null) }) { Text(stringResource(R.string.payments_cancel)) }
    }
}

@Composable
internal fun StarsRow(stars: Int) {
    val label = pluralStringResource(R.plurals.reviews_stars, stars, stars)
    Row(Modifier.clearAndSetSemantics { contentDescription = label }) {
        ReviewRules.STARS.forEach { index ->
            Icon(
                if (index <= stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** `019`: formulario para calificar el salón de una reservación. */
@Composable
fun ReviewForm(
    venueId: String,
    bookingId: String,
    viewModel: ReviewFormViewModel = hiltViewModel(key = "review-$bookingId"),
) {
    val state by viewModel.state.collectAsState()
    ReviewFormContent(state, onEdit = viewModel::edit, onPublish = { viewModel.publish(venueId, bookingId) })
}

@Composable
internal fun ReviewFormContent(
    state: ReviewFormState,
    onEdit: (ReviewFormState) -> Unit,
    onPublish: () -> Unit,
) {
    state.outcome?.let { outcome ->
        Text(
            stringResource(outcome.text()),
            color =
                if (outcome ==
                    ReviewOutcome.FAILED
                ) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    if (!state.open) {
        if (state.outcome == null) {
            VirGoOutlinedButton(onClick = { onEdit(state.copy(open = true)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.reviews_rate_venue))
            }
        }
        return
    }
    Row {
        ReviewRules.STARS.forEach { index ->
            IconButton(onClick = { onEdit(state.copy(stars = index)) }) {
                Icon(
                    if (index <= state.stars) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = pluralStringResource(R.plurals.reviews_stars, index, index),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    val label = stringResource(R.string.reviews_comment)
    VirGoTextField(
        value = state.text,
        onValueChange = { onEdit(state.copy(text = it)) },
        placeholder = label,
        singleLine = false,
        modifier = Modifier.semantics { contentDescription = label },
    )
    VirGoButton(
        onClick = onPublish,
        enabled = state.canPublish,
        text = stringResource(R.string.reviews_publish),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun ReviewOutcome.text(): Int =
    when (this) {
        ReviewOutcome.PUBLISHED -> R.string.reviews_published
        ReviewOutcome.ALREADY -> R.string.reviews_already
        ReviewOutcome.NOT_ALLOWED -> R.string.reviews_not_allowed
        ReviewOutcome.UNAVAILABLE -> R.string.reviews_unavailable
        ReviewOutcome.FAILED -> R.string.reviews_failed
    }
