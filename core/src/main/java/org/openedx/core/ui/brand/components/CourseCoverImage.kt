package org.openedx.core.ui.brand.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.brand

/**
 * Estrategia híbrida de portadas — paridad iOS CourseImageView v2.
 *
 * Cuatro variantes:
 * - [Auto]        Decide por heurística width/height de la imagen recibida.
 * - [Photo]       `ContentScale.Crop` + scrim inferior opcional. Imagen libre landscape.
 * - [Letterbox]   `ContentScale.Fit` sobre fondo crema. Póster vertical con texto embebido.
 * - [Pattern]     [PatternCover] generativo.
 * - [Typographic] Título como portada (fallback final sin URL ni categoría).
 */
sealed interface CourseCoverVariant {
    /** Decisión automática por heurística. Default para uso general. */
    data object Auto : CourseCoverVariant

    data class Photo(val scrim: Boolean = true) : CourseCoverVariant

    data class Letterbox(val background: Color? = null) : CourseCoverVariant

    data object Pattern : CourseCoverVariant

    data class Typographic(val title: String) : CourseCoverVariant
}

/**
 * Heurística para variante `Auto`. **Regla absoluta:** si imageUrl es nulo/vacío → Pattern.
 *
 * Ratio width/height < 1.05 → letterbox (póster vertical SEP con texto embebido).
 * Ratio ≥ 1.05 → photo (imagen libre landscape).
 *
 * Si dimensiones desconocidas, default seguro: letterbox.
 */
private fun resolveAuto(
    imageUrl: String?,
    imageWidth: Int?,
    imageHeight: Int?,
): CourseCoverVariant {
    if (imageUrl.isNullOrBlank()) return CourseCoverVariant.Pattern
    val w = imageWidth
    val h = imageHeight
    if (w == null || h == null || h == 0) return CourseCoverVariant.Letterbox()
    val ratio = w.toFloat() / h.toFloat()
    return if (ratio < 1.05f) CourseCoverVariant.Letterbox()
    else CourseCoverVariant.Photo(scrim = true)
}

/**
 * Composable canónico para portadas de curso. Punto de entrada único — feature modules
 * NO deben usar `AsyncImage` directo.
 *
 * @param courseName Nombre del curso. Va a `contentDescription` (regla absoluta).
 * @param imageUrl URL de la imagen (puede ser null para pattern).
 * @param category Categoría inferida — usada en variante Pattern.
 * @param institutionShort Sigla opcional para PatternCover.
 * @param imageWidth Ancho real de la imagen (px). Usado por heurística Auto.
 * @param imageHeight Alto real de la imagen (px). Usado por heurística Auto.
 * @param variant Variante explícita. Default Auto.
 */
@Composable
fun CourseCoverImage(
    courseName: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    category: CourseCategory = CourseCategory.Educacion,
    institutionShort: String? = null,
    imageWidth: Int? = null,
    imageHeight: Int? = null,
    variant: CourseCoverVariant = CourseCoverVariant.Auto,
) {
    val brand = MaterialTheme.brand
    val resolved = if (variant is CourseCoverVariant.Auto) {
        resolveAuto(imageUrl, imageWidth, imageHeight)
    } else variant

    val containerMod = modifier
        .clip(BrandShapes.Card)
        .semantics { role = Role.Image }

    when (resolved) {
        is CourseCoverVariant.Photo -> {
            Box(modifier = containerMod.background(brand.palette.brandCream)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = courseName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (resolved.scrim) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brand.gradients.scrimBottom),
                    )
                }
            }
        }

        is CourseCoverVariant.Letterbox -> {
            val bg = resolved.background ?: brand.palette.brandCream
            Box(
                modifier = containerMod.background(bg),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = courseName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        CourseCoverVariant.Pattern -> {
            PatternCover(
                category = category,
                institutionShort = institutionShort,
                modifier = containerMod
                    .semantics { role = Role.Image },
            )
        }

        is CourseCoverVariant.Typographic -> {
            Box(
                modifier = containerMod
                    .background(brand.palette.guinda)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = resolved.title.ifBlank { courseName },
                    style = brand.typography.displayLarge,
                    color = brand.palette.textOnHeader,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        CourseCoverVariant.Auto -> Unit // ya resuelto arriba
    }
}
