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
 * 2028-01-28). Los hosts de la API comparten esta cadena (Cloudflare).
 *
 * Los hosts de archivos (files.*) se sirven por Caddy/MinIO y pueden
 * presentar una cadena de Let's Encrypt (root ISRG Root X1/X2) en lugar
 * de GTS -> se les agregan tambien esos pines. OkHttp une los pines de
 * todos los patrones que matchean un host, asi que files.dev.mexicox.gob.mx
 * termina aceptando GTS (via el wildcard *.dev...) e ISRG (via su entrada
 * explicita). Espejo de la domain-config de files.* en
 * network_security_config.xml. Sin esto, FileDownloader (que usa este
 * pinner) fallaria al bajar assets servidos por Caddy con Let's Encrypt.
 *
 * Para regenerar los pines ejecuta: scripts/extract_ssl_pins.ps1
 */
object CertificatePinnerProvider {

    private const val PIN_INTERMEDIATE_GOOGLE_WE1 =
        "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4="

    private const val PIN_ROOT_GTS_R4 =
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="

    // Let's Encrypt: se pinea el ROOT (estable por anios) y no la hoja/
    // intermedia, porque LE rota los certs cada ~60 dias.
    private const val PIN_ROOT_ISRG_X1 =
        "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="

    private const val PIN_ROOT_ISRG_X2 =
        "sha256/diGVwiVYbubAI3RW4hB9xU8e/CH2GnkuvVFZE8zmgzI="

    private val pinnedHosts = listOf(
        "dev.mexicox.gob.mx",
        "*.dev.mexicox.gob.mx",
        "cursos.aprende.gob.mx",
        "*.cursos.aprende.gob.mx",
    )

    // Hosts de archivos servidos por Caddy (camino Let's Encrypt).
    private val fileHosts = listOf(
        "files.dev.mexicox.gob.mx",
        "files.cursos.aprende.gob.mx",
    )

    fun build(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        for (host in pinnedHosts) {
            builder.add(host, PIN_INTERMEDIATE_GOOGLE_WE1, PIN_ROOT_GTS_R4)
        }
        // Cadena Let's Encrypt para los files.* (se une con los pines GTS que
        // ya aportan los patrones wildcard de pinnedHosts).
        for (host in fileHosts) {
            builder.add(host, PIN_ROOT_ISRG_X1, PIN_ROOT_ISRG_X2)
        }
        return builder.build()
    }
}
