package org.openedx.core.ui.theme.brand

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Bolsa de tokens del sistema de diseño @prende.mx.
 *
 * Acceso desde cualquier `@Composable` vía `MaterialTheme.brand`:
 *
 * ```
 * val brand = MaterialTheme.brand
 * Text("Hola", style = brand.typography.displayHero, color = brand.palette.cardPrimary)
 * Spacer(Modifier.height(brand.spacing.l))
 * ```
 */
@Immutable
data class BrandTokens(
    val palette: BrandPaletteHolder,
    val typography: BrandTypography,
    val gradients: BrandGradientsHolder,
)

/**
 * Wrapper inmutable sobre `object BrandPalette` para vivir dentro de `BrandTokens`.
 * Permite — en el futuro — variantes per-flavor del fork sin cambiar el contrato.
 */
@Immutable
data class BrandPaletteHolder(
    val guinda: androidx.compose.ui.graphics.Color = BrandPalette.Guinda,
    val guindaDeep: androidx.compose.ui.graphics.Color = BrandPalette.GuindaDeep,
    val brandGreen: androidx.compose.ui.graphics.Color = BrandPalette.BrandGreen,
    val brandGreenDark: androidx.compose.ui.graphics.Color = BrandPalette.BrandGreenDark,
    val brandGreenLight: androidx.compose.ui.graphics.Color = BrandPalette.BrandGreenLight,
    val brandGreenLighter: androidx.compose.ui.graphics.Color = BrandPalette.BrandGreenLighter,
    val brandCream: androidx.compose.ui.graphics.Color = BrandPalette.BrandCream,
    val brandCreamStrong: androidx.compose.ui.graphics.Color = BrandPalette.BrandCreamStrong,
    val brandHandle: androidx.compose.ui.graphics.Color = BrandPalette.BrandHandle,
    val surfaceWhite: androidx.compose.ui.graphics.Color = BrandPalette.SurfaceWhite,

    val cardPrimary: androidx.compose.ui.graphics.Color = BrandPalette.CardPrimary,
    val cardMedium: androidx.compose.ui.graphics.Color = BrandPalette.CardMedium,
    val cardSecondary: androidx.compose.ui.graphics.Color = BrandPalette.CardSecondary,
    val textInactive: androidx.compose.ui.graphics.Color = BrandPalette.TextInactive,
    val textOnHeader: androidx.compose.ui.graphics.Color = BrandPalette.TextOnHeader,

    val divider: androidx.compose.ui.graphics.Color = BrandPalette.Divider,
    val progressTrack: androidx.compose.ui.graphics.Color = BrandPalette.ProgressTrack,
    val inputBackground: androidx.compose.ui.graphics.Color = BrandPalette.InputBackground,
    val inputStroke: androidx.compose.ui.graphics.Color = BrandPalette.InputStroke,
    val greenTint: androidx.compose.ui.graphics.Color = BrandPalette.GreenTint,
    val greenSoft: androidx.compose.ui.graphics.Color = BrandPalette.GreenSoft,
    val cardStrokeSubtle: androidx.compose.ui.graphics.Color = BrandPalette.CardStrokeSubtle,
    val cardShadowSubtle: androidx.compose.ui.graphics.Color = BrandPalette.CardShadowSubtle,
    val semanticDestructive: androidx.compose.ui.graphics.Color = BrandPalette.SemanticDestructive,

    val pillGreen: androidx.compose.ui.graphics.Color = BrandPalette.PillGreen,
    val pillPink: androidx.compose.ui.graphics.Color = BrandPalette.PillPink,
    val pillYellow: androidx.compose.ui.graphics.Color = BrandPalette.PillYellow,

    val dateToday: androidx.compose.ui.graphics.Color = BrandPalette.DateToday,
    val dateThisWeek: androidx.compose.ui.graphics.Color = BrandPalette.DateThisWeek,
    val dateNextWeek: androidx.compose.ui.graphics.Color = BrandPalette.DateNextWeek,
    val datePastDue: androidx.compose.ui.graphics.Color = BrandPalette.DatePastDue,
    val dateUpcoming: androidx.compose.ui.graphics.Color = BrandPalette.DateUpcoming,
)

@Immutable
data class BrandGradientsHolder(
    val heroDashboard: androidx.compose.ui.graphics.Brush = BrandGradients.HeroDashboard,
    val heroProfile: androidx.compose.ui.graphics.Brush = BrandGradients.HeroProfile,
    val heroGuinda: androidx.compose.ui.graphics.Brush = BrandGradients.HeroGuinda,
    val scrimBottom: androidx.compose.ui.graphics.Brush = BrandGradients.ScrimBottom,
    val fadeToCream: androidx.compose.ui.graphics.Brush = BrandGradients.FadeToCream,
)

/**
 * `CompositionLocal` vacío por defecto — error si no hay envoltura `OpenEdXTheme`.
 * Esto previene uso accidental de `MaterialTheme.brand` fuera de la jerarquía Theme.
 */
val LocalBrand = staticCompositionLocalOf<BrandTokens> {
    error("BrandTokens no provistos. Envuelve el árbol con OpenEdXTheme { ... }")
}

/**
 * Factory canónica de tokens para Cursos @prende.mx.
 * Llamada por `OpenEdXTheme` al envolver el árbol.
 */
fun aprendeBrandTokens(): BrandTokens = BrandTokens(
    palette = BrandPaletteHolder(),
    typography = DefaultBrandTypography,
    gradients = BrandGradientsHolder(),
)

/**
 * Acceso ergonómico desde cualquier `@Composable`:
 * ```
 * val brand = MaterialTheme.brand
 * ```
 */
val MaterialTheme.brand: BrandTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalBrand.current
