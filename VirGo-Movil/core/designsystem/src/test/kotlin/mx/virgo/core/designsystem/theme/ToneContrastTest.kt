package mx.virgo.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** T3503: insignias y burbujas de ícono cumplen AA de texto normal (4.5:1) en ambos temas. */
class ToneContrastTest {
    @Test
    fun lightTonesMeetTextContrast() = assertTones(dark = false)

    @Test
    fun darkTonesMeetTextContrast() = assertTones(dark = true)

    @Test
    fun lightPageBackgroundKeepsMutedTextReadable() {
        assertContrast("muted/soft", LightColorScheme.onSurfaceVariant, VirGoSoft)
        assertContrast("ink/soft", LightColorScheme.onSurface, VirGoSoft)
        assertContrast("primary/soft", LightColorScheme.primary, VirGoSoft)
    }

    private fun assertTones(dark: Boolean) {
        VirGoTone.entries.forEach { tone ->
            val colors = toneColors(tone, dark)
            assertContrast("$tone dark=$dark", colors.content, colors.container)
        }
    }

    private fun assertContrast(
        pair: String,
        foreground: Color,
        background: Color,
    ) {
        assertEquals("$pair must be opaque", 1f, foreground.alpha, 0f)
        assertEquals("$pair must be opaque", 1f, background.alpha, 0f)
        val high = maxOf(foreground.luminance(), background.luminance()) + LUMINANCE_OFFSET
        val low = minOf(foreground.luminance(), background.luminance()) + LUMINANCE_OFFSET
        val ratio = high / low
        assertTrue("$pair contrast $ratio < $AA_TEXT", ratio >= AA_TEXT)
    }
}

private const val AA_TEXT = 4.5f
private const val LUMINANCE_OFFSET = 0.05f
