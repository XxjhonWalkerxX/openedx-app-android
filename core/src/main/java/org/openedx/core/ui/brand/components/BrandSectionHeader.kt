package org.openedx.core.ui.brand.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand

/**
 * Header de sección — paridad iOS BrandSectionHeader.
 *
 * Layout: eyebrow uppercase guinda (opcional) + título grande + acción "Ver todo" (opcional).
 *
 * Ej:
 * ```
 * BrandSectionHeader(
 *     title = "Mis cursos",
 *     eyebrow = "ESTA SEMANA",
 *     actionLabel = "Ver todo",
 *     onActionClick = { ... }
 * )
 * ```
 */
@Composable
fun BrandSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val brand = MaterialTheme.brand
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BrandSpacing.screenHorizontal),
    ) {
        if (eyebrow != null) {
            Text(
                text = eyebrow,
                style = brand.typography.eyebrow,
                color = brand.palette.guinda,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = brand.typography.titleSection,
                color = brand.palette.cardPrimary,
            )
            if (actionLabel != null && onActionClick != null) {
                Text(
                    text = actionLabel,
                    style = brand.typography.bodyEmphasis,
                    color = brand.palette.guinda,
                    modifier = Modifier
                        .clickable(onClick = onActionClick)
                        .padding(start = BrandSpacing.s, top = BrandSpacing.xs, bottom = BrandSpacing.xs),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun BrandSectionHeaderPreview() {
    OpenEdXTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BrandSectionHeader(title = "Mis cursos")
            BrandSectionHeader(
                title = "Recomendados",
                eyebrow = "PARA TI",
                actionLabel = "Ver todo",
                onActionClick = {},
            )
        }
    }
}
