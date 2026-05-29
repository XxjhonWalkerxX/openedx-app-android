package org.openedx.core.security

import okhttp3.CertificatePinner

/**
 * Capa 2 de defensa contra el hallazgo TICDEFENSE #2 (Bypass SSL Pinning).
 *
 * Pinning programatico de OkHttp, redundante con el <pin-set> declarativo
 * de network_security_config.xml. Las dos validaciones viven en codigo
 * distinto (OkHttp's CertificatePinner.check() vs Android's
 * X509TrustManager), asi que un atacante con Frida debe parchear ambas
 * rutas para bypasear el pinning - aumenta el costo del ataque.
 *
 * Pines: SPKI SHA-256 del CA intermediario Google Trust Services WE1
 * (vigente hasta 2029-02-20) y del root GTS Root R4 (backup, hasta
 * 2028-01-28). Ambos dominios del backend comparten esta cadena.
 *
 * Para regenerar los pines ejecuta: scripts/extract_ssl_pins.ps1
 */
object CertificatePinnerProvider {

    private const val PIN_INTERMEDIATE_GOOGLE_WE1 =
        "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4="

    private const val PIN_ROOT_GTS_R4 =
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="

    private val pinnedHosts = listOf(
        "dev.mexicox.gob.mx",
        "*.dev.mexicox.gob.mx",
        "cursos.aprende.gob.mx",
        "*.cursos.aprende.gob.mx",
    )

    fun build(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        for (host in pinnedHosts) {
            builder.add(host, PIN_INTERMEDIATE_GOOGLE_WE1, PIN_ROOT_GTS_R4)
        }
        return builder.build()
    }
}
