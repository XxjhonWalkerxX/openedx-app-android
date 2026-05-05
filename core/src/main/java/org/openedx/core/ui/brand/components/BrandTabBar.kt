package org.openedx.core.ui.brand.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandElevations
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand
import org.openedx.core.ui.theme.brand.rememberBrandHaptics

/**
 * Item del BrandTabBar. `id` es opaco al consumidor, `icon` + `label` definen presentación.
 */
data class BrandTabItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Tab bar pill flotante — paridad iOS spec §4.3.
 *
 * Layout: Row centrado, ancho fit-content, fondo blanco 92% alpha, border negro 8%,
 * shape `BrandShapes.TabPill` 28dp, sombra `BrandElevations.tabBarFloating`.
 *
 * Tab activa: pill `BrandShapes.Pill` con guinda 8% bg + texto/icono guinda.
 * Tab inactiva: texto/icono `textInactive`. Háptico `tabChange()` al tap.
 *
 * Posición flotante: aplica `navigationBarsPadding()` + bottom 16dp internamente.
 * Caller solo posiciona el composable como overlay (Box bottomCenter).
 */
@Composable
fun BrandTabBar(
    items: List<BrandTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.brand
    val haptics = rememberBrandHaptics()

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = BrandElevations.tabBarFloating,
                    shape = BrandShapes.TabPill,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                )
                .clip(BrandShapes.TabPill)
                .background(brand.palette.surfaceWhite.copy(alpha = 0.95f))
                .border(1.dp, Color.Black.copy(alpha = 0.08f), BrandShapes.TabPill)
                .padding(horizontal = BrandSpacing.s, vertical = BrandSpacing.xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BrandSpacing.xs),
        ) {
            items.forEachIndexed { index, item ->
                BrandTabItemView(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = {
                        if (index != selectedIndex) {
                            haptics.tabChange()
                            onSelect(index)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BrandTabItemView(
    item: BrandTabItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val brand = MaterialTheme.brand
    val contentColor = if (selected) brand.palette.guinda else brand.palette.textInactive
    val pillBg = if (selected) brand.palette.guinda.copy(alpha = 0.08f) else Color.Transparent
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .clip(BrandShapes.Pill)
            .background(pillBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = BrandSpacing.m, vertical = BrandSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        if (selected) {
            Spacer(modifier = Modifier.width(BrandSpacing.xs + 2.dp))
            Text(
                text = item.label,
                style = brand.typography.tabLabel,
                color = contentColor,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun BrandTabBarPreview() {
    OpenEdXTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.brand.palette.brandCream)
                .padding(40.dp),
        ) {
            BrandTabBar(
                items = listOf(
                    BrandTabItem("learn", "Aprende", Icons.Outlined.School),
                    BrandTabItem("discover", "Descubre", Icons.Outlined.Explore),
                    BrandTabItem("profile", "Perfil", Icons.Outlined.Person),
                ),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}

