package org.openedx.core.ui.brand.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.brand

/**
 * Stat box vertical — paridad iOS Dashboard hero.
 *
 * Layout vertical: value grande (28sp 800) arriba + label uppercase (10sp 600) abajo.
 * Diseñado para vivir sobre fondos oscuros (heroes verdes).
 */
@Composable
fun BrandStatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onDarkSurface: Boolean = true,
) {
    val brand = MaterialTheme.brand
    val bg = if (onDarkSurface) Color.White.copy(alpha = 0.10f) else brand.palette.greenSoft
    val borderColor = if (onDarkSurface) Color.White.copy(alpha = 0.18f) else brand.palette.divider
    val contentColor = if (onDarkSurface) brand.palette.textOnHeader else brand.palette.cardPrimary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = value,
            style = brand.typography.displayLarge,
            color = contentColor,
        )
        Text(
            text = label.uppercase(),
            style = brand.typography.eyebrow,
            color = contentColor.copy(alpha = 0.78f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1D4D42)
@Composable
private fun BrandStatBoxPreview() {
    OpenEdXTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            BrandStatBox(value = "6", label = "Cursos")
            BrandStatBox(value = "6", label = "Por iniciar")
        }
    }
}
