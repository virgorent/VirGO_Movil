package mx.virgo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Tonos de estado del prototipo (`.ui-badge`, `.ui-row-icon`), ajustados a contraste AA. */
enum class VirGoTone { VIOLET, GREEN, AMBER, GRAY, RED }

data class ToneColors(
    val container: Color,
    val content: Color,
)

private val LightTones =
    mapOf(
        VirGoTone.VIOLET to ToneColors(VirGoPurpleContainer, VirGoPurpleDark),
        VirGoTone.GREEN to ToneColors(Color(0xFFEAF8F1), Color(0xFF13734A)),
        VirGoTone.AMBER to ToneColors(Color(0xFFFFF5DE), Color(0xFF85580F)),
        VirGoTone.GRAY to ToneColors(Color(0xFFEFEFF2), Color(0xFF5B5D66)),
        VirGoTone.RED to ToneColors(Color(0xFFFDECEC), AccessibleError),
    )

private val DarkTones =
    mapOf(
        VirGoTone.VIOLET to ToneColors(Color(0xFF2E2A5C), DarkPrimary),
        VirGoTone.GREEN to ToneColors(Color(0xFF12382A), Color(0xFF8BE0B5)),
        VirGoTone.AMBER to ToneColors(Color(0xFF3D2E0F), Color(0xFFF2C46B)),
        VirGoTone.GRAY to ToneColors(Slate700, Slate50),
        VirGoTone.RED to ToneColors(Color(0xFF4A1C1C), DarkError),
    )

/** Función pura para pruebas de contraste. */
fun toneColors(
    tone: VirGoTone,
    dark: Boolean,
): ToneColors = (if (dark) DarkTones else LightTones).getValue(tone)

@Composable
fun VirGoTone.colors(): ToneColors = toneColors(this, dark = isDarkScheme())

/** Fondo de página del prototipo (`--soft`) en claro; el fondo del esquema en oscuro. */
@Composable
fun pageBackground(): Color = if (isDarkScheme()) MaterialTheme.colorScheme.background else VirGoSoft

@Composable
private fun isDarkScheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < LUMINANCE_MIDPOINT

private const val LUMINANCE_MIDPOINT = 0.5f
