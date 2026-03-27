package org.openedx.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.ttRoundsFamily

private val heroGradient = listOf(
    Color(0xFF1D4D42),
    Color(0xFF2B6959),
    Color(0xFF3D8A72),
)

/**
 * Layout compartido para sub-pantallas de Settings.
 * Aplica el mismo hero verde + banda guinda que SettingsScreen.
 */
@Composable
fun SettingsHeroLayout(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brand_cream)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = heroGradient,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 440f),
                    )
                )
        ) {
            // Banda guinda en el top absoluto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brand_guinda)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsInset()
                    .displayCutoutForLandscape()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = title,
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = brand_cream,
        ) {
            content()
        }
    }
}
