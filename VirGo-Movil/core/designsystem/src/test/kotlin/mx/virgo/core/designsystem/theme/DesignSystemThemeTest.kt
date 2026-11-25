package mx.virgo.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import mx.virgo.core.designsystem.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DesignSystemThemeTest {
    @Test
    fun prototypePalettePreservesExactBrandSwatches() {
        val swatches =
            listOf(
                Triple("purple", Color(0xFF6366F1), VirGoPurple),
                Triple("purpleDark", Color(0xFF4F46E5), VirGoPurpleDark),
                Triple("purpleContainer", Color(0xFFEFEFFF), VirGoPurpleContainer),
                Triple("ink", Color(0xFF18191F), VirGoInk),
                Triple("muted", Color(0xFF747782), VirGoMuted),
                Triple("line", Color(0xFFE9E9ED), VirGoLine),
                Triple("soft", Color(0xFFF7F7F9), VirGoSoft),
                Triple("coral", Color(0xFFFF6B6B), VirGoCoral),
                Triple("success", Color(0xFF21A668), SuccessGreen),
                Triple("error", Color(0xFFEB5757), ErrorRed),
                Triple("warning", Color(0xFFE6A937), WarningYellow),
            )

        swatches.forEach { (name, expected, actual) ->
            assertEquals("Prototype swatch $name must not change for contrast adjustments", expected, actual)
        }
    }

    @Test
    fun allFifteenTypographyRolesUseTheirLocalFontFamily() {
        val headingStyles =
            mapOf(
                "displayLarge" to VirGoTypography.displayLarge,
                "displayMedium" to VirGoTypography.displayMedium,
                "displaySmall" to VirGoTypography.displaySmall,
                "headlineLarge" to VirGoTypography.headlineLarge,
                "headlineMedium" to VirGoTypography.headlineMedium,
                "headlineSmall" to VirGoTypography.headlineSmall,
                "titleLarge" to VirGoTypography.titleLarge,
                "titleMedium" to VirGoTypography.titleMedium,
                "titleSmall" to VirGoTypography.titleSmall,
            )
        val textStyles =
            mapOf(
                "bodyLarge" to VirGoTypography.bodyLarge,
                "bodyMedium" to VirGoTypography.bodyMedium,
                "bodySmall" to VirGoTypography.bodySmall,
                "labelLarge" to VirGoTypography.labelLarge,
                "labelMedium" to VirGoTypography.labelMedium,
                "labelSmall" to VirGoTypography.labelSmall,
            )

        assertEquals("Material 3 typography must cover all 15 roles", 15, headingStyles.size + textStyles.size)
        assertResourceFontFamily(
            "Manrope",
            ManropeFontFamily,
            R.font.manrope_variable,
            setOf(FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold),
        )
        assertResourceFontFamily(
            "DM Sans",
            DmSansFontFamily,
            R.font.dm_sans_variable,
            setOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold),
        )
        headingStyles.forEach { (role, style) ->
            assertEquals("$role must use Manrope", ManropeFontFamily, style.fontFamily)
        }
        textStyles.forEach { (role, style) ->
            assertEquals("$role must use DM Sans", DmSansFontFamily, style.fontFamily)
        }
    }

    @Test
    @Config(sdk = [26, 36])
    fun packagedVariableFontResourcesCanBeOpenedAndLoaded() {
        val resources = RuntimeEnvironment.getApplication().resources
        val fonts =
            mapOf(
                "manrope_variable" to R.font.manrope_variable,
                "dm_sans_variable" to R.font.dm_sans_variable,
            )

        fonts.forEach { (name, resourceId) ->
            assertEquals("$name must be a packaged font resource", "font", resources.getResourceTypeName(resourceId))
            resources.openRawResource(resourceId).use { stream ->
                assertTrue("$name must contain font data", stream.read() != -1)
            }
            assertNotNull("$name must load on the configured Android API", resources.getFont(resourceId))
        }
    }

    @Test
    fun fontLicensesArePackagedForOfflineDistribution() {
        val license =
            RuntimeEnvironment
                .getApplication()
                .assets
                .open("licenses/fonts-ofl.txt")
                .bufferedReader()
                .use { it.readText() }

        listOf("Manrope", "DM Sans", "SIL OPEN FONT LICENSE", "Version 1.1").forEach { requiredText ->
            assertTrue("Packaged font license must contain $requiredText", license.contains(requiredText))
        }
    }

    @Test
    fun lightSchemeUsesAccessibleSemanticVariantsWithoutChangingBrandTokens() {
        assertEquals("Light primary uses the AA brand variant", VirGoPurpleDark, LightColorScheme.primary)
        assertEquals("Light primary text", CardBackground, LightColorScheme.onPrimary)
        assertEquals("Light badge background", VirGoPurpleContainer, LightColorScheme.primaryContainer)
        assertEquals("Light badge text", VirGoPurpleDark, LightColorScheme.onPrimaryContainer)
        assertEquals("Light field background", VirGoSoft, LightColorScheme.surfaceVariant)
        assertEquals("Light placeholder text", AccessibleMuted, LightColorScheme.onSurfaceVariant)
        assertEquals("Light control outline", AccessibleOutline, LightColorScheme.outline)
        assertEquals("Light error outline", AccessibleError, LightColorScheme.error)
    }

    @Test
    fun darkSchemeUsesItsOwnAccessibleSemanticVariants() {
        assertEquals("Dark primary", DarkPrimary, DarkColorScheme.primary)
        assertEquals("Dark primary text", DarkOnPrimary, DarkColorScheme.onPrimary)
        assertEquals("Dark badge background", Slate800, DarkColorScheme.primaryContainer)
        assertEquals("Dark badge text", VirGoPurpleLight, DarkColorScheme.onPrimaryContainer)
        assertEquals("Dark field background", Slate700, DarkColorScheme.surfaceVariant)
        assertEquals("Dark placeholder text", DarkMuted, DarkColorScheme.onSurfaceVariant)
        assertEquals("Dark control outline", DarkOutline, DarkColorScheme.outline)
        assertEquals("Dark error outline", DarkError, DarkColorScheme.error)
    }

    @Test
    fun lightComponentTextPairsMeetNormalTextContrast() {
        assertComponentTextContrast("light", LightColorScheme)
    }

    @Test
    fun darkComponentTextPairsMeetNormalTextContrast() {
        assertComponentTextContrast("dark", DarkColorScheme)
    }

    @Test
    fun lightActiveControlBordersMeetNonTextContrast() {
        assertControlBorderContrast("light", LightColorScheme)
    }

    @Test
    fun darkActiveControlBordersMeetNonTextContrast() {
        assertControlBorderContrast("dark", DarkColorScheme)
    }

    private fun assertResourceFontFamily(
        name: String,
        family: FontFamily,
        resourceId: Int,
        expectedWeights: Set<FontWeight>,
    ) {
        assertTrue("$name must use packaged fonts, not a system fallback", family is FontListFontFamily)
        val fonts = (family as FontListFontFamily).fonts
        assertEquals("$name must declare each required weight once", expectedWeights.size, fonts.size)
        assertEquals("$name supported weights", expectedWeights, fonts.map { it.weight }.toSet())
        fonts.forEach { font ->
            assertTrue("$name must use resource-backed fonts", font is ResourceFont)
            assertEquals("$name must reference its packaged variable font", resourceId, (font as ResourceFont).resId)
            assertEquals(
                "$name weight ${font.weight.weight} must select the variable weight axis",
                FontVariation.Settings(FontVariation.weight(font.weight.weight)),
                font.variationSettings,
            )
        }
    }

    private fun assertComponentTextContrast(
        theme: String,
        scheme: ColorScheme,
    ) {
        with(scheme) {
            assertContrast("$theme primary button onPrimary/primary", onPrimary, primary, 4.5f)
            assertContrast("$theme outlined button and card onSurface/surface", onSurface, surface, 4.5f)
            assertContrast("$theme focused field onSurface/surface", onSurface, surface, 4.5f)
            assertContrast("$theme unfocused/error field onSurface/surfaceVariant", onSurface, surfaceVariant, 4.5f)
            assertContrast("$theme focused placeholder onSurfaceVariant/surface", onSurfaceVariant, surface, 4.5f)
            assertContrast(
                "$theme unfocused/error placeholder onSurfaceVariant/surfaceVariant",
                onSurfaceVariant,
                surfaceVariant,
                4.5f,
            )
            assertContrast(
                "$theme badge onPrimaryContainer/primaryContainer",
                onPrimaryContainer,
                primaryContainer,
                4.5f,
            )
            // T014: the beta uses primary as text colour (beta label, account role) on surface/background.
            assertContrast("$theme primary text/surface", primary, surface, 4.5f)
            assertContrast("$theme primary text/background", primary, background, 4.5f)
            assertContrast("$theme onSurfaceVariant text/background", onSurfaceVariant, background, 4.5f)
            assertContrast("$theme onBackground text/background", onBackground, background, 4.5f)
        }
    }

    private fun assertControlBorderContrast(
        theme: String,
        scheme: ColorScheme,
    ) {
        with(scheme) {
            assertContrast("$theme outlined button outline/surface", outline, surface, 3f)
            assertContrast("$theme unfocused field outline/surfaceVariant", outline, surfaceVariant, 3f)
            assertContrast("$theme focused field primary/surface", primary, surface, 3f)
            assertContrast("$theme error field error/surfaceVariant", error, surfaceVariant, 3f)
            assertContrast("$theme error field error/outer surface", error, surface, 3f)
        }
    }

    private fun assertContrast(
        pair: String,
        foreground: Color,
        background: Color,
        minimum: Float,
    ) {
        assertEquals("$pair foreground must be opaque", 1f, foreground.alpha, 0f)
        assertEquals("$pair background must be opaque", 1f, background.alpha, 0f)
        val foregroundLuminance = foreground.luminance()
        val backgroundLuminance = background.luminance()
        val ratio =
            (maxOf(foregroundLuminance, backgroundLuminance) + 0.05f) /
                (minOf(foregroundLuminance, backgroundLuminance) + 0.05f)

        assertTrue("$pair contrast was $ratio:1; expected at least $minimum:1", ratio >= minimum)
    }
}
