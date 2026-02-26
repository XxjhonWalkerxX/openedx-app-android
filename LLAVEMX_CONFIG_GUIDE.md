# Configuración LlaveMX OAuth - Guía Rápida

## ⚙️ Configuración Inicial

### 1. Credenciales OAuth

Actualizar en `LlaveMxAuthManager.kt`:

```kotlin
companion object {
    // ⚠️ CAMBIAR ESTAS CREDENCIALES
    private const val CLIENT_ID = "tu_client_id_aqui"
    private const val AUTHORIZATION_ENDPOINT = "https://llavemx.sep.gob.mx/oauth/authorize"
    private const val REDIRECT_URI = "mx.aprende.android://oauth/callback"
}
```

### 2. Obtener Client ID

Contactar al equipo de LlaveMX SEP para:
- Client ID
- URL exacta del endpoint de autorización
- Scopes permitidos
- Configurar redirect URI: `mx.aprende.android://oauth/callback`

### 3. Configurar Backend Open edX

El backend debe implementar:

```python
# Ejemplo endpoint Django/Open edX
@api_view(['POST'])
def llavemx_token_exchange(request):
    """
    Exchange LlaveMX authorization code por Open edX tokens.
    
    Recibe:
    - code: authorization code de LlaveMX
    - code_verifier: PKCE verifier (para validar con LlaveMX)
    
    Retorna:
    - access_token: Token de Open edX
    - refresh_token: Token de refresh
    - user: Datos del usuario
    """
    code = request.data.get('code')
    code_verifier = request.data.get('code_verifier')
    
    # 1. Exchange code con LlaveMX
    llavemx_tokens = exchange_code_with_llavemx(code, code_verifier)
    
    # 2. Obtener info de usuario de LlaveMX
    user_info = get_llavemx_user_info(llavemx_tokens['access_token'])
    
    # 3. Crear o vincular cuenta Open edX
    edx_user = create_or_link_user(user_info)
    
    # 4. Generar tokens Open edX
    edx_tokens = generate_edx_tokens(edx_user)
    
    return Response({
        'access_token': edx_tokens['access_token'],
        'refresh_token': edx_tokens['refresh_token'],
        'user': serialize_user(edx_user)
    })
```

### 4. Modificar AuthInteractor (si es necesario)

Si el backend requiere el `code_verifier`, modificar:

```kotlin
// En AuthInteractor.kt
suspend fun loginAuthCode(code: String, codeVerifier: String? = null): Unit {
    // Enviar code_verifier al backend
}
```

Y en `SignInViewModel.kt`:

```kotlin
fun processLlaveMxResult(result: LlaveMxAuthResult?) {
    when (result) {
        is LlaveMxAuthResult.Success -> {
            val codeVerifier = llaveMxAuthManager.getStoredCodeVerifier()
            signInAuthCode(result.authorizationCode, codeVerifier)
        }
        // ...
    }
}
```

## 🧪 Testing sin Backend

Para probar el flujo sin backend completo:

### Opción 1: Mock Server Local

```bash
# Usar ngrok para exponer servidor local
ngrok http 8000

# En LlaveMxAuthManager.kt cambiar temporalmente:
private const val AUTHORIZATION_ENDPOINT = "https://tu-ngrok-url.ngrok.io/oauth/authorize"
```

### Opción 2: Test Deep Link Directamente

```bash
# Simular callback exitoso
adb shell am start -W -a android.intent.action.VIEW \
  -d "mx.aprende.android://oauth/callback?code=TEST_CODE_123&state=CURRENT_STATE"

# Simular callback con error
adb shell am start -W -a android.intent.action.VIEW \
  -d "mx.aprende.android://oauth/callback?error=access_denied&error_description=User%20cancelled"
```

## 🔍 Debugging

### Habilitar Logs Detallados

En `LlaveMxAuthManager.kt` ya están incluidos logs con el tag `LlaveMxAuthManager`.

Filtrar en Logcat:
```
tag:LlaveMx
```

### Inspeccionar URL de Autorización

La URL construida se logea antes de abrir el navegador:
```
LlaveMxAuthManager: URL de autorización construida: https://...
```

Verificar que incluya:
- `response_type=code`
- `client_id=...`
- `redirect_uri=mx.aprende.android://oauth/callback`
- `code_challenge=...`
- `code_challenge_method=S256`
- `state=...`

### Verificar SharedPreferences

Almacenado en: `llavemx_oauth_prefs`

Verificar valores:
```bash
adb shell run-as org.openedx.app cat /data/data/org.openedx.app/shared_prefs/llavemx_oauth_prefs.xml
```

## 🎨 Personalización UI

### Cambiar Color del Botón

En `SignInView.kt`:

```kotlin
OpenEdXButton(
    // ...
    backgroundColor = Color(0xFF611232), // Cambiar este color
)
```

### Cambiar Texto del Botón

En `strings.xml`:

```xml
<string name="llavemx_sign_in">Tu texto personalizado</string>
```

### Cambiar Posición del Botón

Mover el bloque completo en `SignInView.kt` a la ubicación deseada.

## 🚀 Deploy

### Build de Producción

1. Actualizar `CLIENT_ID` con credenciales reales
2. Verificar `AUTHORIZATION_ENDPOINT` correcto
3. Compilar release:
   ```bash
   ./gradlew assembleProdRelease
   ```

### Verificaciones Pre-Deploy

- [ ] Client ID correcto configurado
- [ ] URL de autorización correcta
- [ ] Redirect URI registrado en LlaveMX
- [ ] Deep link testeado
- [ ] Backend implementado y funcionando
- [ ] Logs de debug removidos o deshabilitados
- [ ] ProGuard rules agregadas (si es necesario)

### ProGuard Rules (si se ofusca)

Agregar a `proguard-rules.pro`:

```proguard
# LlaveMX OAuth
-keep class org.openedx.auth.presentation.llavemx.** { *; }
-keepclassmembers class org.openedx.auth.presentation.llavemx.LlaveMxAuthResult** { *; }
```

## 📊 Métricas y Analytics

Agregar tracking de eventos:

```kotlin
// En SignInViewModel.signInLlaveMx()
analytics.logEvent(
    event = "llavemx_auth_started",
    params = emptyMap()
)

// En processLlaveMxResult()
when (result) {
    is LlaveMxAuthResult.Success -> {
        analytics.logEvent("llavemx_auth_success", emptyMap())
    }
    is LlaveMxAuthResult.Error -> {
        analytics.logEvent("llavemx_auth_error", mapOf(
            "error_message" to result.message
        ))
    }
}
```

## 🔐 Seguridad Adicional

### Certificate Pinning (Recomendado)

Agregar pinning para LlaveMX:

```xml
<!-- En res/xml/network_security_config.xml -->
<domain-config>
    <domain includeSubdomains="true">llavemx.sep.gob.mx</domain>
    <pin-set>
        <pin digest="SHA-256">HASH_DEL_CERTIFICADO</pin>
    </pin-set>
</domain-config>
```

### Timeout Handling

Agregar timeout al flujo:

```kotlin
// En SignInViewModel
private var authTimeoutJob: Job? = null

fun signInLlaveMx(context: Context) {
    // ...
    authTimeoutJob = viewModelScope.launch {
        delay(120_000) // 2 minutos
        if (_uiState.value.showProgress) {
            _uiMessage.value = UIMessage.SnackBarMessage("Tiempo de espera agotado")
            _uiState.update { it.copy(showProgress = false) }
        }
    }
}
```

## 📞 Soporte

Para problemas con:
- **OAuth flow**: Revisar logs de `LlaveMxAuthManager`
- **Deep links**: Verificar AndroidManifest
- **Backend**: Revisar implementación del token exchange
- **LlaveMX**: Contactar soporte de SEP

## 🔄 Actualizaciones Futuras

Posibles mejoras:
1. Refresh token automático
2. Biometric authentication después de primer login
3. Multi-cuenta support
4. Logout desde LlaveMX
5. Token revocation

---

**Última actualización**: Febrero 2026

