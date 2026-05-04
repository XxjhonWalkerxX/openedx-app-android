package org.openedx.core.ui.theme.brand

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevaciones canónicas. Para sombras precisas con paridad iOS:
 * `Modifier.shadow(elevation, shape, ambientColor=Color.Black, spotColor=Color.Black)`.
 *
 * Las constantes aquí son guía — la sombra exacta del sistema iOS (negro 4% y=2 blur=8)
 * se replica con `cardSubtle` + `BrandPalette.CardShadowSubtle` como tint.
 */
object BrandElevations {
    /** Card estándar (sombra suave casi imperceptible) */
    val cardSubtle: Dp = 2.dp

    /** Card destacada / hero card flotante */
    val cardRaised: Dp = 6.dp

    /** TabBar flotante (BrandTabBar) */
    val tabBarFloating: Dp = 12.dp

    /** Sticky CTA bottom */
    val stickyCta: Dp = 8.dp
}
