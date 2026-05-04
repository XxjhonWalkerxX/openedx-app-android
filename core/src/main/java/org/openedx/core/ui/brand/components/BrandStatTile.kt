package org.openedx.core.ui.brand.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.brand

/**
 * Stat tile pill — paridad iOS BrandStatTile (hero Dashboard).
 *
 * Píldora con icono pequeño + valor numérico grande + label.
 * Diseñado para vivir sobre fondos oscuros (heroes verdes).
 *
 * Ej: `BrandStatTile(icon = Icons.Filled.School, value = "5", label = "Total")`
 */
@Composable
fun BrandStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onDarkSurface: Boolean = true,
) {
    val brand = MaterialTheme.brand
    val bg = if (onDarkSurface) Color.White.copy(alpha = 0.14f) else brand.palette.greenTint
    val contentColor = if (onDarkSurface) brand.palette.textOnHeader else brand.palette.brandGreen

    Row(
        modifier = modifier
            .clip(BrandShapes.Pill)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = value,
                style = brand.typography.titleCard,
                color = contentColor,
            )
            Text(
                text = label,
                style = brand.typography.caption,
                color = contentColor.copy(alpha = 0.85f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1D4D42)
@Composable
private fun BrandStatTilePreview() {
    OpenEdXTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            BrandStatTile(value = "5", label = "Total", icon = Icons.Filled.PlayArrow)
            BrandStatTile(value = "2", label = "En curso")
            BrandStatTile(value = "1", label = "Sin iniciar")
        }
    }
}
