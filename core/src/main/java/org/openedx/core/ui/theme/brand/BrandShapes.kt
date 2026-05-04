package org.openedx.core.ui.theme.brand

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shapes canónicas del sistema @prende.mx.
 * Paridad iOS `Theme.Sizes` (radii Fibonacci).
 */
object BrandShapes {
    /** Controles, chips simples. iOS 8pt */
    val Control     = RoundedCornerShape(8.dp)

    /** Cards estándar. iOS 12pt → Android 16dp para mejor sensación */
    val Card        = RoundedCornerShape(16.dp)

    /** Containers destacados, hero cards. iOS 20pt */
    val Hero        = RoundedCornerShape(20.dp)

    /** Sheets bottom (top-rounded). iOS 32pt */
    val Sheet       = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    /** Cápsula completa para chips, tabs activos, CTA pill. */
    val Pill        = RoundedCornerShape(percent = 50)

    /** TabBar flotante (BrandTabBar). iOS 28pt */
    val TabPill     = RoundedCornerShape(28.dp)

    /** Chip filtro (Discovery, Course Content tabs) */
    val ChipFilter  = RoundedCornerShape(percent = 50)
}
