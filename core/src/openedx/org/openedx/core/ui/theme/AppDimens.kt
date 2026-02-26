package org.openedx.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dimensiones institucionales EMI
 * Equivalentes a las medidas de iOS convertidas a dp
 *
 * Nota: En iOS 1pt ≈ 1dp en Android para densidad mdpi
 * Para otras densidades, Android escala automáticamente
 */
object AppDimens {

    // ==========================================
    // LOGIN / SIGN IN
    // ==========================================
    /** Logo: maxWidth en iOS = 220 */
    val signInLogoMaxWidth = 220.dp

    /** Logo: maxHeight en iOS = 131 */
    val signInLogoMaxHeight = 131.dp

    /** Header height fraction */
    const val signInHeaderFraction = 0.3f

    // ==========================================
    // COURSE HEADER
    // iOS: 300 → 550 pts (aumentado)
    // En Android usamos un valor proporcional
    // ==========================================
    /** Altura del header de curso expandido */
    val courseHeaderHeightExpanded = 280.dp

    /** Altura del header de curso colapsado */
    val courseHeaderHeightCollapsed = 200.dp

    // ==========================================
    // DASHBOARD COURSE CARD
    // iOS: Banner altura 140 → 350 pts
    // ==========================================
    /** Altura de la imagen en card principal del dashboard */
    val dashboardPrimaryCourseImageHeight = 180.dp

    /** Altura de la imagen en card secundaria del dashboard */
    val dashboardSecondaryCourseImageHeight = 140.dp

    // ==========================================
    // CARDS
    // ==========================================
    /** Radio de esquinas para cards */
    val cardCornerRadius = 12.dp

    /** Elevación de cards */
    val cardElevation = 4.dp

    // ==========================================
    // BUTTONS
    // ==========================================
    /** Altura mínima de botones primarios */
    val buttonMinHeight = 48.dp

    /** Radio de esquinas de botones (iOS: cornerRadius 8) */
    val buttonCornerRadius = 8.dp

    // ==========================================
    // SPACING
    // ==========================================
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val spacingXl = 32.dp

    // ==========================================
    // LLAVE MX SECTION
    // ==========================================
    /** Padding vertical de la sección Llave MX */
    val llaveMxVerticalPadding = 24.dp

    /** Padding horizontal de la sección Llave MX */
    val llaveMxHorizontalPadding = 16.dp

    /** Altura de la imagen Llave MX */
    val llaveMxImageHeight = 120.dp
}

