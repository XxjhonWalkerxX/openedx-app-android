package org.openedx.app.image

import android.net.Uri
import coil.intercept.Interceptor
import coil.request.ImageResult

/**
 * Las fotos de perfil ahora llegan como URLs S3 prefirmadas (bucket privado
 * `openedxprofiles`, remediación TICDEFENSE #5): la ruta del objeto es estable,
 * pero la firma (`?X-Amz-Signature=...&X-Amz-Date=...`) cambia en cada petición
 * y expira a la hora.
 *
 * Por defecto Coil usa la URL completa —firma incluida— como clave de caché, así
 * que sin esto re-descargaría la misma foto en cada carga, no la encontraría
 * offline y produciría parpadeo al refrescar.
 *
 * Este interceptor detecta cualquier URL prefirmada (contiene [SIGNATURE_MARKER])
 * y fija las claves de caché de memoria y disco usando esquema+host+ruta y el
 * fragmento (`#v=...`), sin el query string. Así la misma foto comparte clave
 * entre sesiones aunque la firma cambie, pero al subir una foto nueva el backend
 * cambia el fragmento `#v=<profile_image_uploaded_at>` (ver plugin
 * openedx-security-hardening) y por tanto la clave, forzando la recarga — sin
 * esto, como el nombre del objeto es estable por usuario, Coil mostraría la foto
 * vieja cacheada hasta reiniciar la app. La descarga sigue usando la URL completa
 * (con firma; el fragmento no viaja en la petición HTTP) para que el servidor la
 * autorice. Las URLs normales (miniaturas de curso, imágenes del foro, etc.) no
 * traen firma y no se tocan.
 */
class SignedUrlCacheKeyInterceptor : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val url = when (val data = request.data) {
            is String -> data
            is Uri -> data.toString()
            else -> null
        }

        val stableKey = url?.let(::stableCacheKeyOrNull)
        val newRequest = if (stableKey != null) {
            request.newBuilder()
                .memoryCacheKey(stableKey)
                .diskCacheKey(stableKey)
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }

    /**
     * Devuelve `esquema://host/ruta[#fragmento]` (sin query => sin firma) para
     * una URL prefirmada, o `null` si la URL no está firmada (no debe alterarse).
     *
     * El fragmento (`#v=<profile_image_uploaded_at>`) se conserva a propósito:
     * es el cache-buster que cambia al subir una foto nueva, así la clave de
     * caché cambia y Coil recarga la imagen.
     */
    private fun stableCacheKeyOrNull(url: String): String? {
        if (!url.contains(SIGNATURE_MARKER)) return null
        val uri = Uri.parse(url)
        return buildString {
            append(uri.scheme).append("://")
            append(uri.authority.orEmpty())
            append(uri.path.orEmpty())
            uri.fragment?.takeIf { it.isNotEmpty() }?.let { append("#").append(it) }
        }
    }

    private companion object {
        const val SIGNATURE_MARKER = "X-Amz-Signature"
    }
}
