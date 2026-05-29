package org.openedx.core.config

import com.google.gson.annotations.SerializedName

/**
 * Configuración OAuth de LlaveMX por flavor.
 *
 * Los valores cambian entre entornos:
 *   - dev/stage -> sandbox de LlaveMX (val-llave.infotec.mx)
 *   - prod      -> LlaveMX producción (www.llave.gob.mx)
 *
 * El `redirectUri` NO vive aquí: se deriva de `API_HOST_URL` en
 * [org.openedx.auth.presentation.llavemx.LlaveMxAuthManager] para que siempre
 * apunte al backend del flavor activo.
 */
data class LlaveMxConfig(
    @SerializedName("CLIENT_ID")
    val clientId: String = "",
    @SerializedName("AUTHORIZATION_ENDPOINT")
    val authorizationEndpoint: String = "",
) {
    fun isValid() = clientId.isNotBlank() && authorizationEndpoint.isNotBlank()
}
