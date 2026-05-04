package org.openedx.core.ui.brand.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.brand

/**
 * Portada generativa para cursos sin imagen — paridad iOS PatternCover.
 *
 * Render: fondo color tinte de la categoría + ícono Material grande esquina inferior derecha
 * (alpha 0.18) + eyebrow institución arriba izquierda (opcional).
 *
 * @param category Categoría del curso.
 * @param institutionShort Sigla institucional (ej. "SEP", "UNAM"). Opcional.
 * @param modifier Tamaño determinado por el caller (ej. 200×140dp en card).
 */
@Composable
fun PatternCover(
    category: CourseCategory,
    modifier: Modifier = Modifier,
    institutionShort: String? = null,
) {
    val brand = MaterialTheme.brand
    val gradient = Brush.verticalGradient(
        listOf(
            category.tint,
            category.tint.copy(alpha = 0.85f),
        )
    )

    Box(
        modifier = modifier
            .clip(BrandShapes.Card)
            .background(gradient),
    ) {
        // Glyph decorativo esquina inferior derecha
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
                .size(120.dp),
        )
        // Eyebrow institución
        if (institutionShort != null) {
            Text(
                text = institutionShort.uppercase(),
                style = brand.typography.eyebrow,
                color = category.ink.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 10.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun PatternCoverPreview() {
    OpenEdXTheme {
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 140.dp)
                .padding(16.dp),
        ) {
            PatternCover(
                category = CourseCategory.Seguridad,
                institutionShort = "SEP",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
