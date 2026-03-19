package org.openedx.auth.presentation.llavemx

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.core.content.edit
import org.openedx.core.utils.Logger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Gestor de autenticación OAuth 2.0 con PKCE para LlaveMX.
 *
 * Implementa el flujo Authorization Code con PKCE (Proof Key for Code Exchange)
 * según RFC 7636 para autenticación segura sin client_secret.
 */
class LlaveMxAuthManager(private val context: Context) {

    private val logger = Logger("LlaveMxAuthManager")

    companion object {
        // Configuración OAuth2 de LlaveMX
        private const val CLIENT_ID = "202602091646467055"
        private const val AUTHORIZATION_ENDPOINT = "https://val-llave.infotec.mx/oauth.xhtml"
        
        // Redirect URI registrado en LlaveMX — HTTPS bridge que hace el deep link a la app
        const val REDIRECT_URI = "https://dev.mexicox.gob.mx/mobile/callback"

        // Almacenamiento seguro
        private const val PREFS_NAME = "llavemx_oauth_prefs"    
        private const val KEY_CODE_VERIFIER = "code_verifier"
        private const val KEY_STATE = "state"

        // PKCE
        private const val CODE_VERIFIER_LENGTH = 64
        private const val STATE_LENGTH = 32
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Genera un string aleatorio seguro para code_verifier o state.
     */
    private fun generateRandomString(length: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        ).take(length)
    }

    /**
     * Genera el code_challenge a partir del code_verifier usando SHA-256.
     *
     * @param codeVerifier El code_verifier generado
     * @return code_challenge codificado en Base64 URL-safe
     */
    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return Base64.encodeToString(
            hash,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    /**
     * Genera y almacena de forma segura el code_verifier.
     *
     * @return El code_verifier generado
     */
    private fun generateAndStoreCodeVerifier(): String {
        val codeVerifier = generateRandomString(CODE_VERIFIER_LENGTH)
        prefs.edit {
            putString(KEY_CODE_VERIFIER, codeVerifier)
            apply()
        }
        logger.d { "Code verifier generado y almacenado" }
        return codeVerifier
    }

    /**
     * Genera y almacena el state para protección CSRF.
     *
     * @return El state generado
     */
    private fun generateAndStoreState(): String {
        val state = generateRandomString(STATE_LENGTH)
        prefs.edit {
            putString(KEY_STATE, state)
            apply()
        }
        logger.d { "State generado y almacenado" }
        return state
    }

    /**
     * Recupera el code_verifier almacenado.
     *
     * @return El code_verifier o null si no existe
     */
    fun getStoredCodeVerifier(): String? {
        return prefs.getString(KEY_CODE_VERIFIER, null)
    }

    /**
     * Recupera el state almacenado.
     *
     * @return El state o null si no existe
     */
    fun getStoredState(): String? {
        return prefs.getString(KEY_STATE, null)
    }

    /**
     * Valida que el state recibido coincida con el almacenado.
     *
     * @param receivedState El state recibido en el callback
     * @return true si coincide, false en caso contrario
     */
    fun validateState(receivedState: String?): Boolean {
        val storedState = getStoredState()
        val isValid = receivedState != null && receivedState == storedState

        if (!isValid) {
            logger.e { "State inválido. Recibido: $receivedState, Esperado: $storedState" }
        } else {
            logger.d { "State validado correctamente" }
        }

        return isValid
    }

    /**
     * Limpia los datos almacenados después de completar el flujo.
     */
    fun clearStoredData() {
        prefs.edit {
            remove(KEY_CODE_VERIFIER)
            remove(KEY_STATE)
            apply()
        }
        logger.d { "Datos de OAuth limpiados" }
    }

    /**
     * Construye la URL de autorización completa con todos los parámetros PKCE.
     *
     * @return URI de autorización lista para abrir en navegador
     */
    fun buildAuthorizationUrl(): Uri {
        // Generar code_verifier y derivar code_challenge
        val codeVerifier = generateAndStoreCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        // Generar state para CSRF protection
        val state = generateAndStoreState()

        // Construir URL con parámetros
        // IMPORTANTE: LlaveMX Mobile usa "redirect_url" NO "redirect_uri"
        // NO se envía "scope" en sandbox
        val uri = Uri.parse(AUTHORIZATION_ENDPOINT).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_url", REDIRECT_URI) 
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            // NO incluir scope en sandbox
            .build()

        logger.d { "URL de autorización construida: ${uri.toString()}" }

        return uri
    }

    /**
     * Inicia el flujo de autorización abriendo el navegador.
     *
     * @param context Contexto para lanzar el Intent
     */
    fun startAuthorizationFlow(context: Context) {
        val authUrl = buildAuthorizationUrl()

        val intent = Intent(Intent.ACTION_VIEW, authUrl).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            logger.d { "Navegador abierto para autorización" }
        } catch (e: Exception) {
            logger.e { "Error al abrir navegador: ${e.message}" }
            throw LlaveMxAuthException("No se pudo abrir el navegador para autorización", e)
        }
    }
}

/**
 * Excepción personalizada para errores de LlaveMX OAuth.
 */
class LlaveMxAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

