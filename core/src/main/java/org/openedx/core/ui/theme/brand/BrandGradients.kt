package org.openedx.core.ui.theme.brand

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradientes canónicos del sistema. Se usan en heroes (Dashboard, Profile, Discovery)
 * y scrims sobre portadas.
 */
object BrandGradients {

    /** Hero Dashboard (Aprende): brandGreenDark → brandGreen vertical */
    val HeroDashboard: Brush = Brush.verticalGradient(
        listOf(BrandPalette.BrandGreenDark, BrandPalette.BrandGreen)
    )

    /** Hero Profile: brandGreen → brandGreenDark vertical */
    val HeroProfile: Brush = Brush.verticalGradient(
        listOf(BrandPalette.BrandGreen, BrandPalette.BrandGreenDark)
    )

    /** Hero Guinda (cards featured Discovery): guindaDeep → guinda vertical */
    val HeroGuinda: Brush = Brush.verticalGradient(
        listOf(BrandPalette.GuindaDeep, BrandPalette.Guinda)
    )

    /** Scrim inferior para portadas con foto (legibilidad de texto sobre imagen) */
    val ScrimBottom: Brush = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
    )

    /** Fade desde transparente a brandCream para sticky CTA bottom */
    val FadeToCream: Brush = Brush.verticalGradient(
        listOf(Color.Transparent, BrandPalette.BrandCream)
    )
}
