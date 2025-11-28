package com.example.rota_rapida.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================
// COLOR SCHEME – APENAS TEMA CLARO
// ============================

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueUltraLight,
    onPrimaryContainer = BluePrimary,

    secondary = BluePrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = BlueUltraLight,
    onSecondaryContainer = BluePrimaryLight,

    tertiary = BluePrimaryLight,
    onTertiary = Color.White,
    tertiaryContainer = BlueUltraLight,
    onTertiaryContainer = BluePrimaryLight,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,

    outline = BorderLight,

    error = StatusNaoEntregue,
    onError = Color.White
)

// Mantém a mesma assinatura antiga para não quebrar chamadas,
// mas ignora darkTheme/dynamicColor e sempre usa o tema claro.
@Suppress("UNUSED_PARAMETER")
@Composable
fun RotaRapidaTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Fundo da status bar alinhado com o tema claro
            window.statusBarColor = colorScheme.background.toArgb()

            // Ícones escuros na status bar (tema claro)
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view)
                    .isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
