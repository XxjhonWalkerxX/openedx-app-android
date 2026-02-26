# Estructura de Archivos - Integración LlaveMX OAuth

## 📁 Archivos Creados

```
openedx-app-android/
├── auth/src/main/java/org/openedx/auth/presentation/llavemx/
│   ├── LlaveMxAuthManager.kt          ⭐ NUEVO - Gestor PKCE OAuth
│   └── LlaveMxCallbackActivity.kt     ⭐ NUEVO - Manejo de callback/deep link
│
├── auth/src/main/res/values/
│   └── strings.xml                    ✏️ MODIFICADO - Agregados strings LlaveMX
│
├── LLAVEMX_OAUTH_IMPLEMENTATION.md    ⭐ NUEVO - Documentación técnica completa
├── LLAVEMX_CONFIG_GUIDE.md            ⭐ NUEVO - Guía de configuración
└── LLAVEMX_FILE_STRUCTURE.md          ⭐ NUEVO - Este archivo
```

## 📝 Archivos Modificados

### 1. auth/src/main/java/org/openedx/auth/presentation/signin/

```
SignInFragment.kt          ✏️ MODIFICADO
├── Línea 17: + import LlaveMxCallbackActivity
├── Líneas 45-51: + Observador LiveData LlaveMx
├── Línea 119: + object LlaveMxSignIn : AuthEvent
└── Líneas 67-69: + Manejo evento LlaveMxSignIn

SignInViewModel.kt         ✏️ MODIFICADO
├── Líneas 18-20: + imports LlaveMx
├── Línea 63: + parámetro llaveMxAuthManager
├── Líneas 98-118: + método processLlaveMxResult()
└── Líneas 120-136: + método signInLlaveMx()
```

### 2. auth/src/main/java/org/openedx/auth/presentation/signin/compose/

```
SignInView.kt              ✏️ MODIFICADO
├── Líneas 319-330: + Botón LlaveMX en UI
```

### 3. app/src/main/java/org/openedx/app/di/

```
ScreenModule.kt            ✏️ MODIFICADO
├── Línea 10: + import LlaveMxAuthManager
├── Línea 104: + factory { LlaveMxAuthManager(get()) }
└── Línea 132: + get() en SignInViewModel constructor
```

### 4. app/src/main/

```
AndroidManifest.xml        ✏️ MODIFICADO
├── Líneas 79-91: + Activity declaration para LlaveMxCallbackActivity
└── Líneas 83-90: + intent-filter para deep link
```

## 🔍 Resumen de Cambios por Módulo

### Módulo: `auth`

**Nuevos:**
- `llavemx/LlaveMxAuthManager.kt` (203 líneas)
- `llavemx/LlaveMxCallbackActivity.kt` (195 líneas)

**Modificados:**
- `signin/SignInFragment.kt` (+8 líneas)
- `signin/SignInViewModel.kt` (+42 líneas)
- `signin/compose/SignInView.kt` (+15 líneas)
- `res/values/strings.xml` (+4 líneas)

### Módulo: `app`

**Modificados:**
- `di/ScreenModule.kt` (+3 líneas)
- `AndroidManifest.xml` (+16 líneas)

### Documentación

**Nuevos:**
- `LLAVEMX_OAUTH_IMPLEMENTATION.md` (documentación técnica)
- `LLAVEMX_CONFIG_GUIDE.md` (guía de configuración)
- `LLAVEMX_FILE_STRUCTURE.md` (este archivo)

## 📊 Estadísticas

| Tipo | Cantidad |
|------|----------|
| Archivos nuevos | 5 |
| Archivos modificados | 7 |
| Líneas de código agregadas | ~488 |
| Módulos afectados | 2 (auth, app) |

## 🎯 Puntos de Integración

### 1. UI → ViewModel
```
SignInView.kt
  ↓ onEvent(AuthEvent.LlaveMxSignIn)
SignInFragment.kt
  ↓ viewModel.signInLlaveMx(requireContext())
SignInViewModel.kt
```

### 2. ViewModel → Manager
```
SignInViewModel.kt
  ↓ llaveMxAuthManager.startAuthorizationFlow()
LlaveMxAuthManager.kt
  ↓ Genera PKCE + Abre navegador
```

### 3. Callback → ViewModel
```
LlaveMX (external)
  ↓ Redirect con code
LlaveMxCallbackActivity.kt
  ↓ LiveData.postValue()
SignInFragment.kt (observa)
  ↓ viewModel.processLlaveMxResult()
SignInViewModel.kt
  ↓ signInAuthCode()
AuthInteractor.kt
```

## 🔗 Dependencias Añadidas

### Koin (Inyección de Dependencias)
```kotlin
// En ScreenModule.kt
factory { LlaveMxAuthManager(get()) }
```

### Android Components
- `androidx.lifecycle.LiveData` (ya existente)
- `androidx.activity.ComponentActivity` (ya existente)
- `androidx.compose.*` (ya existente)

**No se requieren nuevas dependencias en build.gradle**

## 🎨 Recursos Visuales

### Colores Usados
- `#611232` - Guinda institucional (botón LlaveMX)
- Colores del tema Material (iconos success/error)

### Strings Agregados
- `llavemx_sign_in`
- `llavemx_auth_success`
- `llavemx_auth_error`

### Test Tags
- `btn_llavemx` - Botón de LlaveMX en login

## 🧪 Flujo de Testing

### 1. Test de Compilación
```bash
./gradlew :auth:assembleDebug
./gradlew :app:assembleDebug
```

### 2. Test de Deep Link
```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "mx.aprende.android://oauth/callback?code=TEST&state=ABC"
```

### 3. Test de UI
- Abrir Sign In screen
- Verificar botón "Iniciar sesión con LlaveMX"
- Pulsar botón
- Verificar apertura de navegador

## 📱 Compatibilidad

- **Min SDK**: Heredado del proyecto (24)
- **Target SDK**: Heredado del proyecto (34)
- **Compose**: Compatible con versión actual
- **Kotlin**: Compatible con versión actual

## 🔐 Seguridad Implementada

### PKCE (RFC 7636)
- ✅ code_verifier: SecureRandom 64 chars
- ✅ code_challenge: SHA-256 + Base64 URL-safe
- ✅ code_challenge_method: S256

### CSRF Protection
- ✅ state: SecureRandom 32 chars
- ✅ Validación en callback

### Storage
- ✅ SharedPreferences MODE_PRIVATE
- ✅ Auto-cleanup después del flujo

## 🚀 Próximos Pasos para Deploy

1. ✏️ Actualizar CLIENT_ID en `LlaveMxAuthManager.kt`
2. ✏️ Verificar AUTHORIZATION_ENDPOINT
3. 🔧 Implementar backend token exchange
4. 🧪 Testing end-to-end con LlaveMX real
5. 📊 Agregar analytics (opcional)
6. 🔒 Agregar certificate pinning (opcional)
7. 📦 Build release

## 📞 Mantenimiento

### Logs a Monitorear
- Tag: `LlaveMxAuthManager`
- Tag: `LlaveMxCallbackActivity`
- Tag: `SignInViewModel` (mensajes de LlaveMX)

### Errores Comunes
1. **"Class not found"**: Verificar AndroidManifest
2. **"State inválido"**: Limpiar SharedPreferences
3. **"No se abre navegador"**: Verificar Intent.ACTION_VIEW

---

**Última actualización**: Febrero 2026  
**Versión**: 1.0.0

