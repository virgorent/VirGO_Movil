package mx.virgo.app.ui.guest

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import mx.virgo.app.R

/** Imágenes ilustrativas únicamente para salones de muestra; nunca representan al catálogo real. */
@Composable
internal fun VenuePhotoContent(
    venue: VenueSample,
    modifier: Modifier = Modifier,
) {
    val image =
        if (!venue.venueId.startsWith(SAMPLE_VENUE_ID_PREFIX)) {
            null
        } else {
            when (venue.categories.firstOrNull()) {
                CATEGORY_GARDENS -> R.drawable.venue_cat_jardines
                CATEGORY_POOL -> R.drawable.venue_cat_alberca
                CATEGORY_ELEGANT -> R.drawable.venue_cat_elegante
                CATEGORY_HACIENDAS -> R.drawable.venue_cat_haciendas
                CATEGORY_KIDS -> R.drawable.venue_cat_infantil
                else -> null
            }
        }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(imageVector = Icons.Filled.Place, contentDescription = null)
        }
    }
}
