package org.openedx.auth.presentation.llavemx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.openedx.auth.R
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.utils.Logger

/**
 * Activity que maneja el callback OAuth de LlaveMX.
 *
 * Captura el deep link mx.aprende.android://oauth/callback,
 * extrae el authorization code y state, valida el state,
 * y comunica el resultado a través de un LiveData observable.
 */
class LlaveMxCallbackActivity : ComponentActivity() {

    private val authManager: LlaveMxAuthManager by inject()
    private val logger = Logger("LlaveMxCallbackActivity")

    // Objeto companion para observar resultados desde otros componentes
    companion object {
        private val _authResult = MutableLiveData<LlaveMxAuthResult?>()
        val authResult: LiveData<LlaveMxAuthResult?> = _authResult

        /**
         * Limpia el resultado anterior.
         */
        fun clearResult() {
            _authResult.value = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logger.d { "LlaveMxCallbackActivity iniciada" }

        // Procesar el intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualizar el intent para singleTop
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data == null) {
            logger.e { "Intent sin datos de URI" }
            setErrorResult("No se recibieron datos de autorización")
            return
        }

        logger.d { "Deep link recibido: $data" }

        // Extraer parámetros
        val code = data.getQueryParameter("code")
        val state = data.getQueryParameter("state")
        val error = data.getQueryParameter("error")
        val errorDescription = data.getQueryParameter("error_description")

        when {
            error != null -> {
                logger.e { "Error en OAuth: $error - $errorDescription" }
                setErrorResult(errorDescription ?: error)
            }
            code.isNullOrBlank() -> {
                logger.e { "Code no recibido" }
                setErrorResult("No se recibió el código de autorización")
            }
            !authManager.validateState(state) -> {
                logger.e { "State inválido - Posible ataque CSRF" }
                setErrorResult("Error de seguridad: state inválido")
            }
            else -> {
                logger.d { "Authorization code recibido exitosamente" }
                // Obtener code_verifier ANTES de limpiar los datos
                val codeVerifier = authManager.getStoredCodeVerifier()
                if (codeVerifier != null) {
                    // ⚡ CRÍTICO: Procesar INMEDIATAMENTE para evitar que expire el código
                    // Los códigos de LlaveMX expiran en ~1 minuto, por lo que cualquier
                    // delay artificial puede causar que el intercambio PKCE falle
                    setSuccessResult(code, codeVerifier)
                } else {
                    logger.e { "code_verifier no encontrado en almacenamiento" }
                    setErrorResult("Error: code_verifier no encontrado")
                }
            }
        }
    }

    private fun setSuccessResult(code: String, codeVerifier: String) {
        // ⚡ Procesar inmediatamente sin delay para evitar que expire el código
        _authResult.value = LlaveMxAuthResult.Success(code, codeVerifier)
        authManager.clearStoredData()
        
        // Mostrar UI brevemente (opcional)
        setContent {
            OpenEdXTheme {
                CallbackResultScreen(
                    isSuccess = true,
                    message = stringResource(R.string.llavemx_auth_success),
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    private fun setErrorResult(errorMessage: String) {
        // ⚡ Procesar inmediatamente sin delay
        _authResult.value = LlaveMxAuthResult.Error(errorMessage)
        authManager.clearStoredData()
        
        // Mostrar UI brevemente (opcional)
        setContent {
            OpenEdXTheme {
                CallbackResultScreen(
                    isSuccess = false,
                    message = errorMessage,
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Pantalla de resultado del callback.
 */
@Composable
private fun CallbackResultScreen(
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        // Auto-cerrar rápidamente (reducido de 1500ms a 300ms)
        // Solo para dar feedback visual sin retrasar el proceso
        delay(300)
        onDismiss()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSuccess) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colors.error,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Resultado de la autenticación con LlaveMX.
 */
sealed class LlaveMxAuthResult {
    data class Success(
        val authorizationCode: String,
        val codeVerifier: String
    ) : LlaveMxAuthResult()
    data class Error(val message: String) : LlaveMxAuthResult()
}


