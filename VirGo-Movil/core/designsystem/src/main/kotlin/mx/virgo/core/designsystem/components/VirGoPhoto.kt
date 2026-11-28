package mx.virgo.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val PhotoShape = RoundedCornerShape(12.dp)
private const val PHOTO_RATIO = 16f / 9f

/** Solo URLs `https` (`036` RF-05); cualquier otra cuenta como «sin foto». */
fun safePhotoUrl(url: String?): String? =
    url?.trim()?.takeIf { it.length > HTTPS.length && it.startsWith(HTTPS, ignoreCase = true) }

/** Las fotos que sí se pueden mostrar, en el orden del servidor (la primera es la portada). */
fun safePhotoUrls(urls: List<String>): List<String> = urls.mapNotNull(::safePhotoUrl)

/**
 * Foto con respaldo (`036` RF-04): debajo siempre hay un fondo neutro con el ícono, y la imagen lo
 * cubre cuando carga. Si la URL falla o no hay foto, queda el ícono; nunca un hueco ni un error.
 * `contentDescription = null` la vuelve decorativa.
 */
@Composable
fun VirGoPhoto(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = Icons.Outlined.Storefront,
) {
    val safe = safePhotoUrl(url)
    val semantics =
        if (contentDescription == null) {
            Modifier.clearAndSetSemantics {}
        } else {
            Modifier.semantics { this.contentDescription = contentDescription }
        }
    Box(
        modifier
            .clip(PhotoShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(semantics),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            fallbackIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FALLBACK_ICON_DP.dp),
        )
        if (safe != null) {
            AsyncImage(
                model = safe,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().testTag(PHOTO_IMAGE_TAG),
            )
        }
    }
}

/** Portada 16:9 de una tarjeta. */
@Composable
fun VirGoCoverPhoto(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) = VirGoPhoto(url, contentDescription, modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO))

/**
 * Carrusel 16:9 con indicador «2 de 5» (`036` RF-02). `pageLabel(n, total)` da el texto del indicador
 * y `photoDescription(n, total)` lo que anuncia TalkBack por foto (RF-06). Sin fotos, el respaldo.
 */
@Composable
fun VirGoPhotoPager(
    urls: List<String>,
    pageLabel: @Composable (Int, Int) -> String,
    photoDescription: @Composable (Int, Int) -> String,
    modifier: Modifier = Modifier,
) {
    val photos = safePhotoUrls(urls)
    if (photos.isEmpty()) {
        VirGoCoverPhoto(null, null, modifier)
        return
    }
    val pager = rememberPagerState { photos.size }
    Box(modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO)) {
        HorizontalPager(pager, Modifier.fillMaxSize(), pageSpacing = 8.dp) { page ->
            VirGoPhoto(photos[page], photoDescription(page + 1, photos.size), Modifier.fillMaxSize())
        }
        if (photos.size > 1) {
            Surface(
                shape = PhotoShape,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = INDICATOR_ALPHA),
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            ) {
                Text(
                    pageLabel(pager.currentPage + 1, photos.size),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/** Etiqueta de la imagen cargada, para pruebas. */
const val PHOTO_IMAGE_TAG = "virgo-photo-image"

private const val HTTPS = "https://"
private const val FALLBACK_ICON_DP = 32
private const val INDICATOR_ALPHA = 0.8f
