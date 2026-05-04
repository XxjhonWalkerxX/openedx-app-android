package org.openedx.core.ui.brand.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.brand

/**
 * Anillo de progreso circular — paridad iOS BrandProgressRing.
 *
 * @param progress Valor 0f..1f. Valores fuera del rango se clampan.
 * @param size Diámetro del ring.
 * @param strokeWidth Grosor del trazo.
 * @param trackColor Color del track inactivo.
 * @param progressColor Color del progreso. Por defecto Guinda.
 * @param showLabel Si muestra "X%" centrado.
 */
@Composable
fun BrandProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 8.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    showLabel: Boolean = true,
) {
    val brand = MaterialTheme.brand
    val track = trackColor ?: brand.palette.progressTrack
    val fill = progressColor ?: brand.palette.guinda

    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = 600),
        label = "BrandProgressRing.progress",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sw = strokeWidth.toPx()
            val arcSize = Size(this.size.width - sw, this.size.height - sw)
            val topLeft = Offset(sw / 2f, sw / 2f)
            // Track
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            // Progress
            drawArc(
                color = fill,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
        }
        if (showLabel) {
            Text(
                text = "${(clamped * 100).toInt()}%",
                style = brand.typography.titleSection,
                color = brand.palette.cardPrimary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)
@Composable
private fun BrandProgressRingPreview() {
    OpenEdXTheme {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            BrandProgressRing(progress = 0.78f)
        }
    }
}
