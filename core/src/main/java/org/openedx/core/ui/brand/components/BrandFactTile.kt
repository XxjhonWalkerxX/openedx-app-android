package org.openedx.core.ui.brand.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand

/**
 * Tile de dato (icono + label uppercase + valor) — paridad iOS BrandFactTile.
 *
 * Usado en hero Profile (MIEMBRO DESDE / PAÍS / AÑO NAC.) y stats Course Detail
 * (DURACIÓN / LECCIONES / CONSTANCIA / IDIOMA).
 *
 * Diseñado para fondos claros (cards blancas o cream).
 */
@Composable
fun BrandFactTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val brand = MaterialTheme.brand

    Column(
        modifier = modifier
            .clip(BrandShapes.Card)
            .background(brand.palette.surfaceWhite)
            .padding(horizontal = BrandSpacing.l, vertical = BrandSpacing.m),
        verticalArrangement = Arrangement.spacedBy(BrandSpacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = brand.palette.brandGreen,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.size(BrandSpacing.xs))
            }
            Text(
                text = label.uppercase(),
                style = brand.typography.eyebrow,
                color = brand.palette.cardSecondary,
            )
        }
        Spacer(Modifier.height(BrandSpacing.xs))
        Text(
            text = value,
            style = brand.typography.titleCard,
            color = brand.palette.cardPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun BrandFactTilePreview() {
    OpenEdXTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BrandSpacing.s),
            modifier = Modifier.padding(BrandSpacing.l),
        ) {
            BrandFactTile(label = "Miembro desde", value = "Mar 2024", icon = Icons.Filled.CalendarMonth)
            BrandFactTile(label = "País", value = "MX", icon = Icons.Filled.Public)
            BrandFactTile(label = "Año nac.", value = "1990", icon = Icons.Filled.School)
        }
    }
}
