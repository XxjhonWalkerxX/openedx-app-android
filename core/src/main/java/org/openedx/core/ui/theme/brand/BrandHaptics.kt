package org.openedx.core.ui.theme.brand

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * API de hápticos del sistema @prende.mx.
 * Mapeo iOS → Android:
 *
 * | Evento iOS                 | Android constante                 |
 * |----------------------------|-----------------------------------|
 * | `.impact(.soft)`           | `CONTEXT_CLICK`                    |
 * | `.impact(.medium)`         | `CONFIRM` (fallback `LONG_PRESS`) |
 * | `.selection()`             | `CLOCK_TICK`                       |
 * | `.notification(.success)`  | `CONFIRM`                          |
 * | `.notification(.error)`    | `REJECT` (fallback `LONG_PRESS`)  |
 *
 * Regla: máximo un háptico por interacción. NUNCA en scroll.
 */
interface BrandHapticsApi {
    /** Tap en card / row de lista */
    fun cardTap()

    /** CTA primario (Continuar curso, Iniciar curso, Submit quiz) */
    fun ctaPrimary()

    /** Cambio de tab, selección de chip, opción de quiz */
    fun tabChange()

    /** Resultado positivo (lección completada, respuesta correcta) */
    fun success()

    /** Resultado negativo (respuesta incorrecta, error red) */
    fun error()
}

private class BrandHapticsImpl(private val view: View) : BrandHapticsApi {

    override fun cardTap() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    override fun ctaPrimary() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }

    override fun tabChange() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    override fun success() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }

    override fun error() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }
}

/**
 * Recuerda una instancia de [BrandHapticsApi] vinculada a la View Compose actual.
 * Uso típico:
 *
 * ```
 * val haptics = rememberBrandHaptics()
 * Button(onClick = { haptics.ctaPrimary(); onConfirm() }) { ... }
 * ```
 */
@Composable
fun rememberBrandHaptics(): BrandHapticsApi {
    val view = LocalView.current
    return remember(view) { BrandHapticsImpl(view) }
}
