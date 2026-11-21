package mx.virgo.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

internal val LightColorScheme =
    lightColorScheme(
        primary = VirGoPurpleDark,
        onPrimary = CardBackground,
        primaryContainer = VirGoPurpleContainer,
        onPrimaryContainer = VirGoPurpleDark,
        secondary = AccessibleMuted,
        onSecondary = CardBackground,
        tertiary = VirGoCoral,
        onTertiary = VirGoInk,
        tertiaryContainer = VirGoCoralContainer,
        onTertiaryContainer = VirGoInk,
        background = CardBackground,
        onBackground = Slate900,
        surface = CardBackground,
        onSurface = Slate900,
        surfaceVariant = InputBackground,
        onSurfaceVariant = Slate500,
        outline = AccessibleOutline,
        outlineVariant = VirGoLine,
        error = AccessibleError,
        onError = CardBackground,
        errorContainer = VirGoCoralContainer,
        onErrorContainer = AccessibleError,
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = Slate800,
        onPrimaryContainer = VirGoPurpleLight,
        secondary = Slate200,
        onSecondary = Slate900,
        tertiary = VirGoPurpleLight,
        onTertiary = DarkOnPrimary,
        tertiaryContainer = Slate700,
        onTertiaryContainer = Slate50,
        background = Slate900,
        onBackground = Slate50,
        surface = Slate800,
        onSurface = Slate50,
        surfaceVariant = Slate700,
        onSurfaceVariant = DarkMuted,
        outline = DarkOutline,
        outlineVariant = Slate700,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = AccessibleError,
        onErrorContainer = VirGoCoralContainer,
    )

@Composable
fun VirGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val spacing = Spacing()

    CompositionLocalProvider(
        LocalSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VirGoTypography,
            shapes = VirGoShapes,
            content = content,
        )
    }
}
