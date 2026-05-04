package org.openedx.core.ui.brand.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.Icon
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.brand

/**
 * Chip de filtro / categoría — paridad iOS BrandChip.
 *
 * Tres variantes:
 * - [BrandChipStyle.Solid]   — fondo guinda, texto blanco. Estado activo.
 * - [BrandChipStyle.Outline] — fondo blanco, borde sutil, texto cardPrimary. Inactivo.
 * - [BrandChipStyle.Tonal]   — fondo greenTint, texto brandGreen. Estado informativo.
 */
enum class BrandChipStyle { Solid, Outline, Tonal }

@Composable
fun BrandChip(
    label: String,
    modifier: Modifier = Modifier,
    style: BrandChipStyle = BrandChipStyle.Outline,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val brand = MaterialTheme.brand

    val (background, contentColor, border) = when (style) {
        BrandChipStyle.Solid -> Triple(
            brand.palette.guinda,
            brand.palette.textOnHeader,
            null as BorderStroke?,
        )
        BrandChipStyle.Outline -> Triple(
            brand.palette.surfaceWhite,
            brand.palette.cardPrimary,
            BorderStroke(1.dp, brand.palette.cardStrokeSubtle),
        )
        BrandChipStyle.Tonal -> Triple(
            brand.palette.greenTint,
            brand.palette.brandGreen,
            null as BorderStroke?,
        )
    }

    val rowMod = modifier
        .clip(BrandShapes.ChipFilter)
        .background(background)
        .let { if (border != null) it.border(border, BrandShapes.ChipFilter) else it }
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 14.dp, vertical = 8.dp)

    Row(
        modifier = rowMod,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        Text(
            text = label,
            color = contentColor,
            style = brand.typography.tabLabel,
        )
        Spacer(Modifier.width(0.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun BrandChipPreview() {
    OpenEdXTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            BrandChip(label = "Todos", style = BrandChipStyle.Solid)
            BrandChip(label = "Populares", style = BrandChipStyle.Outline)
            BrandChip(label = "En curso", style = BrandChipStyle.Tonal)
        }
    }
}
