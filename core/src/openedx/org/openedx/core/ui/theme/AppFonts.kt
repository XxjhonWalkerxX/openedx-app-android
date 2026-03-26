@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package org.openedx.core.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.openedx.core.R

// ── TT Rounds Neue Trial Variable ────────────────────────────────────────────
// Un solo archivo TTF contiene todos los ejes: weight (100–900) y width (50–200).
// Eje wdth=100 → Normal | wdth=50 → Compressed
//
// Jerarquía del sistema:
//   ttRoundsFamily          → cuerpo / labels (wdth normal, pesos múltiples)
//   ttRoundsCompressedMedium → títulos de plataforma (wdth 50, weight 500)
//   ttRoundsCompressedThin   → decorativo / eyebrows (wdth 50, weight 100, italic)
// ─────────────────────────────────────────────────────────────────────────────

/** Familia global — reemplaza Inter en pantallas re-themed. */
val ttRoundsFamily = FontFamily(
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Thin,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.ExtraLight,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    // Italic
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Medium,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.width(100f)),
    ),
)

/** Compressed Medium — titulares de plataforma, secciones, cards. */
val ttRoundsCompressedMedium = FontFamily(
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
)

/** Compressed Thin Italic — eyebrows, etiquetas decorativas, subtítulos secundarios. */
val ttRoundsCompressedThinItalic = FontFamily(
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Thin,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Light,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
    // Versión upright para cuando no se quiere italic
    Font(
        resId = R.font.tt_rounds_neue_variable,
        weight = FontWeight.Thin,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(50f)),
    ),
)
