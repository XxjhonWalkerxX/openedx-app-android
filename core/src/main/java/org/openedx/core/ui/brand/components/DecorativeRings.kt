package org.openedx.core.ui.brand.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Anillos decorativos animados — paridad iOS DecorativeRings.
 *
 * Rotación lenta (60s/vuelta = ~6°/seg) sobre fondos hero (Dashboard, Profile, Discovery).
 * Sin estado de presentación — solo decoración.
 *
 * Performance: usa `rememberInfiniteTransition` con `LinearEasing` para no acumular drift.
 * Cuando el composable sale de composición, la animación se detiene automáticamente
 * (Compose cancela el transition).
 *
 * @param ringColor Color de los anillos (default blanco 8% alpha — buen contraste sobre verde/guinda).
 * @param ringCount Número de anillos. Default 4.
 * @param baseRadiusFraction Radio del primer anillo como fracción del menor lado.
 * @param spacingFraction Espacio entre anillos como fracción del menor lado.
 */
@Composable
fun DecorativeRings(
    modifier: Modifier = Modifier,
    ringColor: Color = Color.White.copy(alpha = 0.08f),
    ringCount: Int = 4,
    baseRadiusFraction: Float = 0.18f,
    spacingFraction: Float = 0.10f,
    strokeWidthPx: Float = 1.5f,
) {
    val transition = rememberInfiniteTransition(label = "DecorativeRings.rotation")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "DecorativeRings.angle",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val minSide = minOf(w, h)
        // Centro desplazado a la derecha — paridad iOS center.x = 0.85, center.y = 0.4
        val cx = w * 0.85f
        val cy = h * 0.40f

        rotate(degrees = angle, pivot = Offset(cx, cy)) {
            for (i in 1..ringCount) {
                val r = minSide * (baseRadiusFraction + (i - 1) * spacingFraction)
                drawCircle(
                    color = ringColor,
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidthPx),
                )
            }
        }
    }
}
