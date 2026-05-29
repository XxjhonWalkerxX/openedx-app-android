package org.openedx.app.data.networking

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.openedx.core.data.model.ErrorResponse
import org.openedx.core.system.EdxError

class HandleErrorInterceptor(
    private val gson: Gson
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        return if (isErrorResponse(response)) {
            val jsonStr = response.body?.string()
            if (jsonStr != null) handleErrorResponse(response, jsonStr) else response
        } else {
            response
        }
    }

    private fun isErrorResponse(response: Response): Boolean {
        return response.code in 400..500 && response.body != null
    }

    private fun handleErrorResponse(response: Response, jsonStr: String): Response {
        return try {
            val errorResponse = gson.fromJson(jsonStr, ErrorResponse::class.java)
            handleParsedErrorResponse(errorResponse) ?: rebuildResponse(response, jsonStr)
        } catch (e: JsonSyntaxException) {
            // El cuerpo de error no es JSON (p.ej. una pagina HTML de error del
            // servidor). No es un fallo de la app: devolvemos la respuesta HTTP
            // original para que las capas superiores manejen el codigo de estado,
            // en lugar de propagar una excepcion y cerrar la aplicacion.
            rebuildResponse(response, jsonStr)
        }
    }

    /**
     * El cuerpo de una [Response] solo puede leerse una vez y aqui ya se
     * consumio con `body.string()`. Reconstruimos la respuesta con el mismo
     * contenido para que las capas superiores puedan volver a leerlo.
     */
    private fun rebuildResponse(response: Response, body: String): Response {
        return response.newBuilder()
            .body(body.toResponseBody(response.body?.contentType()))
            .build()
    }

    private fun handleParsedErrorResponse(errorResponse: ErrorResponse?): Response? {
        val exception = when {
            errorResponse?.error == ERROR_INVALID_GRANT -> EdxError.InvalidGrantException()
            errorResponse?.error == ERROR_USER_NOT_ACTIVE -> EdxError.UserNotActiveException()
            errorResponse?.errorDescription != null ->
                EdxError.ValidationException(errorResponse.errorDescription.orEmpty())

            else -> return null
        }
        throw exception
    }

    companion object {
        const val ERROR_INVALID_GRANT = "invalid_grant"
        const val ERROR_USER_NOT_ACTIVE = "user_not_active"
    }
}
