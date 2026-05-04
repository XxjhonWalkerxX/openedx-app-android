package org.openedx.core.ui.theme.brand

import androidx.compose.ui.graphics.Color

/**
 * Tokens de paleta institucional Cursos @prende.mx.
 * Fuente única de hex literales del fork — ningún feature module debe redefinir colores.
 *
 * Paridad iOS: ver `Theme.Colors` en openedx-app-ios y `ANDROID_DESIGN_SPEC.md` §1.
 */
object BrandPalette {

    // Institucionales (base absoluta — no cambiar)
    val Guinda            = Color(0xFF611232)
    val GuindaDeep        = Color(0xFF3E0A20)
    val BrandGreen        = Color(0xFF2B6959)
    val BrandGreenDark    = Color(0xFF1D4D42)
    val BrandGreenLight   = Color(0xFF3D8A72)
    val BrandGreenLighter = Color(0xFF5BB89A)
    val BrandCream        = Color(0xFFECE9E4)
    val BrandCreamStrong  = Color(0xFFDFD4C2)
    val BrandHandle       = Color(0xFFC8C3BA)
    val SurfaceWhite      = Color(0xFFFFFFFF)

    // Texto
    val CardPrimary       = Color(0xFF1C1B18)
    val CardMedium        = Color(0xFF5A5650)
    val CardSecondary     = Color(0xFF9A9590)
    val TextInactive      = Color(0xFFAAAAAA)
    val TextOnHeader      = Color(0xFFFFFFFF)

    // UI
    val Divider           = Color(0xFFF0ECE8)
    val ProgressTrack     = Color(0xFFE5E0D8)
    val InputBackground   = Color(0xFFF7F6F2)
    val InputStroke       = Color(0xFFDBD6CE)
    val GreenTint         = Color(0x1F2B6959) // BrandGreen 12%
    val GreenSoft         = Color(0x0F2B6959) // BrandGreen 6%
    val CardStrokeSubtle  = Color(0x0A000000) // Black 4%
    val CardShadowSubtle  = Color(0x0A000000) // Black 4%
    val SemanticDestructive = Color(0xFF8C1430)

    // Pills / badges
    val PillGreen         = Color(0xFF9ADBC8)
    val PillPink          = Color(0xFFF0A0B0)
    val PillYellow        = Color(0xFFF5DC80)

    // Dates timeline (paridad iOS Course Dates tab)
    val DateToday         = Guinda
    val DateThisWeek      = BrandGreen
    val DateNextWeek      = BrandCreamStrong
    val DatePastDue       = SemanticDestructive
    val DateUpcoming      = CardSecondary
}
