# Integración LlaveMX OAuth con PKCE - Open edX Android

## 📋 Resumen

Esta implementación agrega autenticación OAuth 2.0 con PKCE (Proof Key for Code Exchange) para LlaveMX en la aplicación Open edX Android.

## 🏗️ Arquitectura

### Archivos Creados

1. **`LlaveMxAuthManager.kt`**
   - **Ubicación**: `auth/src/main/java/org/openedx/auth/presentation/llavemx/`
   - **Propósito**: Gestiona el flujo PKCE completo
   - **Responsabilidades**:
     - Genera `code_verifier` aleatorio (64 caracteres)
     - Calcula `code_challenge` usando SHA-256
     - Genera `state` para protección CSRF
     - Almacena credenciales de forma segura en SharedPreferences
     - Construye URL de autorización
     - Inicia el navegador para autorización

2. **`LlaveMxCallbackActivity.kt`**
   - **Ubicación**: `auth/src/main/java/org/openedx/auth/presentation/llavemx/`
   - **Propósito**: Maneja el deep link de callback OAuth
   - **Responsabilidades**:
     - Captura deep link `mx.aprende.android://oauth/callback`
     - Extrae `code` y `state` de la URL
     - Valida el `state` contra ataque CSRF
     - Comunica resultado via LiveData
     - Muestra UI de feedback (éxito/error)

### Archivos Modificados

1. **`SignInFragment.kt`**
   - Agregado evento `LlaveMxSignIn` al sealed interface `AuthEvent`
   - Agregado manejo del evento en el `when` statement
   - Agregado observador de `LlaveMxCallbackActivity.authResult`

2. **`SignInViewModel.kt`**
   - Agregado parámetro `llaveMxAuthManager` al constructor
   - Agregado método `signInLlaveMx()` para iniciar flujo
   - Agregado método `processLlaveMxResult()` para procesar callback
   - Usa método existente `signInAuthCode()` para el token exchange

3. **`SignInView.kt`**
   - Agregado botón "Iniciar sesión con LlaveMX" en la UI
   - Estilo: fondo guinda institucional (#611232)
   - Ubicación: Después del botón de Sign In principal

4. **`ScreenModule.kt`** (DI)
   - Agregado `factory { LlaveMxAuthManager(get()) }`
   - Agregado `get()` adicional en constructor de `SignInViewModel`

5. **`AndroidManifest.xml`** (app module)
   - Agregada declaración de `LlaveMxCallbackActivity`
   - Configurado intent-filter para deep link OAuth

6. **`strings.xml`** (auth module)
   - `llavemx_sign_in`: "Iniciar sesión con LlaveMX"
   - `llavemx_auth_success`: "Autenticación exitosa"
   - `llavemx_auth_error`: "Error en la autenticación"

## 🔐 Flujo de Autenticación

```
┌─────────────────┐
│  Usuario pulsa  │
│  botón LlaveMX  │
└────────┬────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ SignInViewModel.signInLlaveMx()        │
│ - Muestra loading                      │
│ - Llama a LlaveMxAuthManager           │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ LlaveMxAuthManager                     │
│ 1. Genera code_verifier (random 64ch) │
│ 2. Calcula code_challenge (SHA256)    │
│ 3. Genera state (random 32ch)         │
│ 4. Almacena verifier + state          │
│ 5. Construye URL con parámetros       │
│ 6. Abre navegador                     │
└────────┬───────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Usuario autentica en LlaveMX       │
│  (navegador externo)                │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  LlaveMX redirige a:                │
│  mx.aprende.android://oauth/callback│
│  ?code=XXX&state=YYY                │
└────────┬────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ LlaveMxCallbackActivity (deep link)   │
│ 1. Captura code y state               │
│ 2. Valida state                       │
│ 3. Publica resultado en LiveData      │
│ 4. Limpia datos stored                │
│ 5. Se cierra automáticamente          │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ SignInFragment observa resultado       │
│ - Llama viewModel.processLlaveMxResult│
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ SignInViewModel.processLlaveMxResult() │
│ - Llama signInAuthCode(code)          │
└────────┬───────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│ AuthInteractor.loginAuthCode()         │
│ - Exchange code por tokens            │
│ - Guarda sesión                       │
│ - Navega a Dashboard                  │
└────────────────────────────────────────┘
```

## 🔧 Configuración Requerida

### 1. Actualizar credenciales OAuth

Editar `LlaveMxAuthManager.kt`:

```kotlin
companion object s
    private const val CLIENT_ID = "202602091646467055"
    private const val AUTHORIZATION_ENDPOINT = "https://llavemx.sep.gob.mx/oauth/authorize"
    private const val REDIRECT_URI = "mx.aprende.android://oauth/callback"
}
```

### 2. Configurar Backend Open edX

El backend debe:
- Aceptar el authorization code de LlaveMX
- Realizar token exchange con LlaveMX
- Retornar access_token de Open edX
- El método `AuthInteractor.loginAuthCode()` ya existe y maneja esto

### 3. Probar Deep Link

Desde terminal:

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "mx.aprende.android://oauth/callback?code=test123&state=abc456"
```

## 🔒 Seguridad Implementada

### PKCE (RFC 7636)
- ✅ `code_verifier`: 64 caracteres aleatorios
- ✅ `code_challenge`: SHA-256 del verifier
- ✅ `code_challenge_method`: S256
- ✅ Sin necesidad de `client_secret`

### CSRF Protection
- ✅ `state`: 32 caracteres aleatorios
- ✅ Validación de state en callback
- ✅ Rechazo de requests con state inválido

### Almacenamiento Seguro
- ✅ SharedPreferences en modo privado
- ✅ Limpieza automática después del flujo
- ✅ No expone credenciales en logs (solo en debug)

## 📱 UI/UX

### Botón de LlaveMX
- **Color**: Guinda institucional `#611232`
- **Texto**: Blanco
- **Ubicación**: Después del botón "Sign In"
- **Test tag**: `btn_llavemx`

### Feedback Visual
- **Loading**: CircularProgressIndicator durante el flujo
- **Éxito**: Ícono verde + mensaje + auto-cierre (1.5s)
- **Error**: Ícono rojo + mensaje de error + auto-cierre

## 🧪 Testing

### Test Manual

1. **Flujo completo**:
   ```
   1. Abrir app
   2. Ir a Sign In
   3. Pulsar "Iniciar sesión con LlaveMX"
   4. Verificar apertura de navegador
   5. Autenticar en LlaveMX
   6. Verificar redirección a app
   7. Verificar login exitoso
   ```

2. **Validar PKCE**:
   - Inspeccionar URL de autorización en Logcat
   - Verificar presencia de `code_challenge` y `code_challenge_method=S256`

3. **Validar CSRF**:
   - Intentar callback con state inválido
   - Verificar rechazo

### Logs Útiles

Filtrar en Logcat:
```
tag:LlaveMxAuthManager OR tag:LlaveMxCallbackActivity
```

## 🚀 Próximos Pasos

Esta implementación **NO incluye**:

1. ❌ Token exchange en backend (requiere integración servidor)
2. ❌ Refresh token handling
3. ❌ User info fetch desde LlaveMX
4. ❌ Mapeo de usuario LlaveMX → Open edX

### Para Completar la Integración

1. **Backend**: Implementar endpoint que:
   ```
   POST /api/llavemx/token-exchange
   Body: { "code": "xxx", "code_verifier": "yyy" }
   Returns: { "access_token": "...", "user": {...} }
   ```

2. **Android**: Modificar `AuthInteractor.loginAuthCode()` para:
   - Enviar `code_verifier` junto con `code`
   - Manejar respuesta del backend
   - Crear/vincular cuenta Open edX

## 📋 Checklist de Implementación

- ✅ Clase `LlaveMxAuthManager` con generación PKCE
- ✅ `LlaveMxCallbackActivity` con deep link handling
- ✅ Evento `LlaveMxSignIn` en `AuthEvent`
- ✅ Botón UI en pantalla de login
- ✅ Integración con `SignInViewModel`
- ✅ Inyección de dependencias (Koin)
- ✅ Configuración AndroidManifest
- ✅ Strings localizados
- ✅ Validación de state (CSRF)
- ✅ Almacenamiento seguro de credenciales
- ✅ Manejo de errores
- ✅ Feedback visual
- ❌ Integración backend completa (pendiente)

## 🐛 Troubleshooting

### El navegador no se abre
- Verificar que el device tenga navegador instalado
- Revisar permisos en AndroidManifest
- Revisar logs: `LlaveMxAuthManager: Error al abrir navegador`

### Deep link no funciona
- Verificar configuración en AndroidManifest
- Probar con adb shell (ver sección "Probar Deep Link")
- Verificar que el scheme sea `mx.aprende.android`

### State inválido
- Verificar que la URL de callback incluya el state correcto
- Revisar que no haya múltiples instancias de la app
- Limpiar SharedPreferences: Ajustes → Apps → Open edX → Borrar datos

### Build fails
- Sync Gradle
- Clean + Rebuild
- Verificar todas las importaciones

## 📝 Notas de Arquitectura

### Por qué esta estructura:

1. **Módulo auth**: LlaveMX es un método de autenticación, pertenece aquí
2. **LiveData compartido**: Permite comunicación Activity → Fragment sin acoplamiento
3. **Reutiliza signInAuthCode()**: Aprovecha flujo OAuth existente
4. **Koin**: Sigue patrón DI del proyecto
5. **Compose UI**: Alineado con migración a Compose del proyecto

### Merge-safe:

- ✅ No modifica lógica core
- ✅ Archivos nuevos en package propio
- ✅ Cambios mínimos en archivos existentes
- ✅ Compatible con upstream Open edX

## 📄 Licencia

Mismo que Open edX Android (AGPL-3.0)

---

**Autor**: Implementación PKCE LlaveMX  
**Fecha**: Febrero 2026  
**Versión**: 1.0.0

