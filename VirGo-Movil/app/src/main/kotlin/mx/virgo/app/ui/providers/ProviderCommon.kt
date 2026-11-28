package mx.virgo.app.ui.providers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.EmptyState
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoRowDivider
import mx.virgo.core.designsystem.components.VirGoSection
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.domain.VenueReviews

@Composable
internal fun ComingSoon(text: Int) = EmptyState(title = stringResource(text), iconVector = Icons.Outlined.Handshake)

@Composable
internal fun ReviewsSection(reviews: VenueReviews?) {
    VirGoSection(title = stringResource(R.string.providers_reviews)) {
        if (reviews == null || reviews.reviews.isEmpty()) {
            Text(stringResource(R.string.providers_reviews_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@VirGoSection
        }
        Text(stringResource(R.string.providers_reviews_summary, reviews.rating, reviews.count))
        VirGoCard {
            Column(Modifier.padding(horizontal = 16.dp)) {
                reviews.reviews.forEachIndexed { index, review ->
                    if (index > 0) VirGoRowDivider()
                    VirGoRow(
                        title = stringResource(R.string.providers_review_title, review.authorName, review.stars),
                        subtitle = review.text,
                        icon = Icons.Outlined.Star,
                        tone = VirGoTone.AMBER,
                        standalone = false,
                        subtitleMaxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
    }
}

internal fun categoryLabel(category: ProviderCategory): Int =
    when (category) {
        ProviderCategory.CATERING -> R.string.provider_category_catering
        ProviderCategory.MUSIC -> R.string.provider_category_music
        ProviderCategory.PHOTO -> R.string.provider_category_photo
        ProviderCategory.DECOR -> R.string.provider_category_decor
        ProviderCategory.FURNITURE -> R.string.provider_category_furniture
    }

@Composable
internal fun pricingText(pricing: ProviderPricing): String =
    stringResource(
        if (pricing ==
            ProviderPricing.PER_PERSON
        ) {
            R.string.provider_per_person
        } else {
            R.string.provider_per_event
        },
    )
