package mx.virgo.core.designsystem.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import mx.virgo.core.designsystem.theme.VirGoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class VirGoPhotoTest {
    @get:Rule
    val compose = createComposeRule()

    // Dominio reservado: nunca resuelve, así la prueba no depende de la red.
    private val photos = listOf(1, 2, 3).map { "https://fotos.invalid/$it.jpg" }

    @Test
    fun onlyHttpsUrlsAreShown() {
        assertEquals("https://a.mx/1.jpg", safePhotoUrl(" https://a.mx/1.jpg "))
        assertNull(safePhotoUrl("http://a.mx/1.jpg"))
        assertNull(safePhotoUrl("https://"))
        assertNull(safePhotoUrl(""))
        assertNull(safePhotoUrl(null))
        assertEquals(listOf("https://a.mx/2.jpg"), safePhotoUrls(listOf("ftp://a.mx/1.jpg", "https://a.mx/2.jpg")))
    }

    @Test
    fun withoutPhotosOrWithAnUnsafeUrlNoImageIsRequested() {
        compose.setContent {
            VirGoTheme {
                VirGoPhotoPager(listOf("http://a.mx/1.jpg"), { p, t -> "$p de $t" }, { p, t -> "Foto $p de $t" })
            }
        }
        compose.onAllNodesWithTag(PHOTO_IMAGE_TAG, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun pagerAnnouncesEachPhotoAndUpdatesTheIndicator() {
        compose.setContent {
            VirGoTheme {
                VirGoPhotoPager(photos, { p, t -> "$p de $t" }, { p, t -> "Foto $p de $t de Los Encinos" })
            }
        }
        compose.onNodeWithText("1 de 3").assertIsDisplayed()
        compose.onNodeWithContentDescription("Foto 1 de 3 de Los Encinos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Foto 1 de 3 de Los Encinos").performTouchInput { swipeLeft() }
        compose.onNodeWithText("2 de 3").assertIsDisplayed()
        compose.onNodeWithContentDescription("Foto 2 de 3 de Los Encinos").assertIsDisplayed()
    }
}
