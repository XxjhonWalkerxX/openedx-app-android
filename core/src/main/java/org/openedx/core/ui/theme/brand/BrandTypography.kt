package org.openedx.core.ui.theme.brand

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.openedx.core.R

/**
 * Familia Noto Sans bundleada (paridad iOS Cursos @prende.mx).
 * Fuente: Google Fonts — descarga libre.
 * Pesos: Regular(400), Medium(500), SemiBold(600), Bold(700) + Italic.
 */
val NotoSans: FontFamily = FontFamily(
    Font(R.font.noto_sans_regular,            FontWeight.Normal,   FontStyle.Normal),
    Font(R.font.noto_sans_medium,             FontWeight.Medium,   FontStyle.Normal),
    Font(R.font.noto_sans_semi_bold,          FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.noto_sans_bold,               FontWeight.Bold,     FontStyle.Normal),
    Font(R.font.noto_sans_italic,             FontWeight.Normal,   FontStyle.Italic),
    Font(R.font.noto_sans_medium_italic,      FontWeight.Medium,   FontStyle.Italic),
    Font(R.font.noto_sans_semi_bold_italic,   FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.noto_sans_bold_italic,        FontWeight.Bold,     FontStyle.Italic),
)

/**
 * Roles tipográficos semánticos.
 *
 * Equivalencia iOS pt → Android sp (paridad):
 *   30pt Bold → displayHero 28sp
 *   24pt Bold → displayLarge 24sp
 *   18pt SemiBold → titleSection 17sp
 *   15pt SemiBold → titleCard 14sp
 *   14pt Regular → body 13sp
 *   12pt Medium → caption 11sp
 *   13pt Medium → tabLabel 12sp
 *   11pt Medium uppercase → eyebrow 10sp
 */
@Immutable
data class BrandTypography(
    val displayHero: TextStyle,
    val displayLarge: TextStyle,
    val titleSection: TextStyle,
    val titleCard: TextStyle,
    val body: TextStyle,
    val bodyEmphasis: TextStyle,
    val caption: TextStyle,
    val eyebrow: TextStyle,
    val tabLabel: TextStyle,
    val ctaLabel: TextStyle,
)

/** Instancia por defecto del sistema — usada por `aprendeBrandTokens()`. */
val DefaultBrandTypography: BrandTypography = BrandTypography(
    displayHero = TextStyle(
        fontFamily = NotoSans,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
    ),
    displayLarge = TextStyle(
        fontFamily = NotoSans,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleSection = TextStyle(
        fontFamily = NotoSans,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleCard = TextStyle(
        fontFamily = NotoSans,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    body = TextStyle(
        fontFamily = NotoSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyEmphasis = TextStyle(
        fontFamily = NotoSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    caption = TextStyle(
        fontFamily = NotoSans,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
    ),
    eyebrow = TextStyle(
        fontFamily = NotoSans,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.0.sp,
    ),
    tabLabel = TextStyle(
        fontFamily = NotoSans,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
    ),
    ctaLabel = TextStyle(
        fontFamily = NotoSans,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)
