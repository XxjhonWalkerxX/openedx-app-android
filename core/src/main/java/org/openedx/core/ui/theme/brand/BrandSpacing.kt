package org.openedx.core.ui.theme.brand

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing canónico — grid 4dp.
 * Paridad iOS `Theme.Sizes.horizontalPadding` (20pt).
 *
 * Uso: `BrandSpacing.l` para gaps estándar entre elementos.
 * Nunca hardcodear `.dp` en feature modules — usar tokens aquí.
 */
object BrandSpacing {
    val xs: Dp   = 4.dp
    val s: Dp    = 8.dp
    val m: Dp    = 12.dp
    val l: Dp    = 16.dp
    val xl: Dp   = 20.dp
    val xxl: Dp  = 24.dp
    val xxxl: Dp = 32.dp

    /** Padding horizontal estándar de pantallas post-login. iOS 20pt */
    val screenHorizontal: Dp = 20.dp

    /** Separación vertical entre secciones grandes */
    val sectionVertical: Dp = 24.dp
}
