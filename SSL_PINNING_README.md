# Remediación SSL Pinning — Vulnerabilidad #2 TICDEFENSE

Documento de trabajo para la respuesta al dictamen TICDEFENSE Cybersecurity
del **04-mayo-2026** (oficio DGTIC UAF/713/DGTIC/DSIyPR/324/2026) sobre la
aplicación **Cursos @prende.mx** Android.

| Campo                | Valor                                    |
|----------------------|------------------------------------------|
| Aplicación           | Cursos @prende.mx                        |
| Versión              | 1.0.1                                    |
| Repo                 | openedx-app-android (fork)               |
| Rama de implementación | `feature/ssl-pinning-defense`         |
| Rama de evidencia    | `security-test/ssl-pinning-replication` |
| Vulnerabilidad       | #2 — Bypass SSL Pinning (Severidad MEDIO) |
| Fecha límite         | 2026-05-30                               |

---

## 1. Contexto general — Estado de las 5 vulnerabilidades

| #   | Vulnerabilidad                           | Severidad | Donde se resuelve              | Estado          |
|-----|------------------------------------------|-----------|--------------------------------|-----------------|
| 1   | Weak Lock Out Mechanism                  | MEDIO     | Backend (plugin Open edX)      | ✅ Implementado |
| 2   | **Bypass SSL Pinning**                   | MEDIO     | **Cliente Android (este doc)** | 🔄 En progreso  |
| 3   | Mass assignment                          | INFO/Bajo | Backend (defensa en profundidad) | ✅ Implementado |
| 4   | Activación sin verificación de correo    | MEDIO     | Backend (upgrade Tutor 21.0.6) | ✅ Cerrado      |
| 5   | Imágenes de perfil públicas              | BAJO      | Infraestructura (bucket MinIO) | ⏭️ Pendiente    |

El plugin del backend para #1 + #3 + #4 vive en
`C:\Users\JDNICOLAS\Documents\openedx\openedx-security-hardening`
(versión 1.0.5, instalado vía host venv + container).

---

## 2. Hallazgo del dictamen — Vulnerabilidad #2

Texto literal del dictamen (paráfrasis):

> La implementación actual de SSL Pinning puede ser evadida mediante
> herramientas de instrumentación dinámica como SSL Unpinning 2.0 (basada
> en Frida). Esto permite interceptar el tráfico HTTPS de la aplicación,
> representando un riesgo medio.

### Diagnóstico real (después de auditar el código)

El dictamen es **impreciso pero el riesgo de fondo es real**:

- La app **no tiene SSL Pinning de ningún tipo**:
  - No hay `android:networkSecurityConfig` declarado en `AndroidManifest.xml`.
  - No hay `CertificatePinner` configurado en ninguno de los 3
    `OkHttpClient` que tiene el proyecto:
    - `app/src/main/java/org/openedx/app/di/NetworkingModule.kt:27`
    - `core/src/main/java/org/openedx/core/module/download/FileDownloader.kt:16`
    - `core/src/main/java/org/openedx/core/module/TranscriptManager.kt:29`
- La única protección que tenía la app era pasiva: `targetSdk 36` →
  Android no confía en CAs instaladas por el usuario por default. Eso es
  bypaseado por **cualquiera con root, Magisk o Frida** (no se requiere
  Frida explícitamente para vulnerar — basta con root o MDM).

### Restricciones de contexto

- Distribución por **APK directo** (no Play Store, no MDM).
- Por eso **NO aplican**: Play Integrity API, mTLS con cert distribuido vía MDM.
- Backend con **Cloudflare al frente** (`server: cloudflare`, `cf-ray`)
  emitiendo certs vía **Google Trust Services** (no Let's Encrypt directo).

---

## 3. Replicación de la vulnerabilidad — Evidencia para el oficio

### 3.1 Setup del entorno (Windows)

Realizado en PC Windows con cel Motorola G31 (Android 12) conectado por USB.

```powershell
# Instalación mitmproxy (MSI oficial https://mitmproxy.org/downloads/)
mitmproxy --version  # validación

# Túnel USB (la PC y el cel no estaban en la misma subred)
adb reverse tcp:8080 tcp:8080

# Levantar el proxy
mitmweb --listen-host 127.0.0.1 --listen-port 8080
```

En el cel:
- Proxy manual → `127.0.0.1:8080`.
- Instalado el cert raíz de mitmproxy via `http://mitm.it` → Ajustes →
  Encriptación y credenciales → Instalar certificado CA.

### 3.2 Problema inicial detectado

Con el APK oficial instalado, la app rechazaba la conexión con error:

```
javax.net.ssl.SSLHandshakeException:
java.security.cert.CertPathValidatorException:
Trust anchor for certification path not found.
```

Esto NO es por pinning, es porque `targetSdk 36` ignora CAs de usuario.
Para demostrar la **ausencia de pinning real**, hubo que construir un
APK debug con `<debug-overrides>`.

### 3.3 Branch de evidencia

Rama `security-test/ssl-pinning-replication` (commit `f2e250e`,
**NO mergear a `theme-cursos`/`main`**):

**Archivo nuevo:** `app/src/main/res/xml/network_security_config_debug.xml`

```xml
<network-security-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

`<debug-overrides>` **solo aplica si `android:debuggable=true`** (APKs
`*Debug`). En `*Release` Android lo ignora completamente — equivalente
controlado a un dispositivo con root/Magisk que puede agregar CAs al
trust store.

**Manifest modificado** apunta a ese XML solo en esa rama.

### 3.4 Resultado de la intercepción

Con el APK debug instalado y mitmproxy activo, **toda la comunicación HTTPS
fue interceptada y registrada**.

#### Evidencia A — Credenciales en texto claro

```http
POST https://dev.mexicox.gob.mx/oauth2/access_token/ HTTP/2.0
content-type: application/x-www-form-urlencoded

grant_type=password
client_id=zrxzez1eCwVzjNNHzuerPmt5uso7guriA2TpqDiN
username=admin-tests
password=***REDACTED***                  ← password en claro
token_type=JWT
```

#### Evidencia B — Tokens de larga vida

Response del mismo endpoint:

```json
{
    "access_token": "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.<JWT>",
    "expires_in": 3600,
    "token_type": "JWT",
    "scope": "read write email profile user_id",
    "refresh_token": "***REDACTED***"     ← refresh_token (válido ~14 días)
}
```

Cookie de sesión Django también interceptable, `Max-Age=1209600` (14 días).

#### Evidencia C — PII expuesta en endpoint /accounts/

`GET /api/user/v1/accounts/admin-tests` devuelve `mailing_address`,
`name`, `username`, `user_id`, fechas de creación/último login.

#### Evidencia D (no parte del dictamen) — Flujo OAuth LlaveMX comprometido

Adicionalmente al hallazgo TICDEFENSE original, se observó que **el flujo
OAuth completo de LlaveMX (val-llave.infotec.mx) es interceptable**,
incluyendo:

- El `code` JWT del callback `dev.mexicox.gob.mx/mobile/callback?code=...`
- El POST de intercambio `api/mobile/llavemx/login/`
- El CURP del usuario en URLs (`/api/mobile/v0.5/users/<CURP>`)

Un atacante puede secuestrar la sesión sin necesidad de saber la
contraseña LlaveMX del usuario.

### 3.5 Acciones de mitigación post-evidencia

⚠️ **Pendientes inmediatas (independientes de la implementación):**

- [ ] Rotar password de `admin-tests` (quedó expuesto en mitmproxy local).
- [ ] Revocar `refresh_token` capturado del usuario `admin-tests`:
  ```bash
  tutor local exec lms ./manage.py lms shell -c \
    "from oauth2_provider.models import RefreshToken; \
     RefreshToken.objects.filter(token='<TOKEN>').delete()"
  ```
- [ ] Crear usuario `pentest_user` dedicado para pruebas futuras (no usar admin).
- [ ] Eliminar cert de mitmproxy del cel cuando termine la batería de tests.

---

## 4. Arquitectura de defensa propuesta — 3 capas

**Principio**: defensa en profundidad. Ninguna capa es individualmente
"Frida-proof", pero combinadas elevan el costo del ataque de **5 minutos**
a **horas/días** y requieren acceso físico al dispositivo.

```
┌──────────────────────────────────────────────────────────────────┐
│  CAPA 1 — Network Security Configuration (declarativa)           │
│                                                                  │
│  network_security_config.xml con <pin-set> + bloqueo de CAs      │
│  de usuario. Validación a nivel del sistema operativo Android.   │
│                                                                  │
│  Bloquea: MITM con CA de usuario, mitmproxy/Burp/Charles, MDM    │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  CAPA 2 — OkHttp CertificatePinner (programática)                │
│                                                                  │
│  Validación duplicada en código distinto al del sistema.         │
│  CertificatePinnerProvider compartido por los 3 OkHttpClient.    │
│                                                                  │
│  Frida tiene que parchear DOS rutas independientes               │
│  para bypasear las capas 1 y 2.                                  │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│  CAPA 3 — RASP (Runtime Application Self-Protection)             │
│                                                                  │
│  freeRASP (Talsec Community, MIT) detecta:                       │
│    - Frida en memoria                                            │
│    - Root / Magisk / SuperSU                                     │
│    - Hooking (Xposed, LSPosed)                                   │
│    - Debugger, emulador, tamper, malware                         │
│                                                                  │
│  Respuesta: bloquear login + telemetría (Opción B).              │
└──────────────────────────────────────────────────────────────────┘
```

### 4.1 Estrategia de pinning (capa 1 y 2)

Tras analizar la cadena SSL real con `scripts/extract_ssl_pins.ps1`:

| Dominio                   | Cert hoja             | CA intermediaria        | Root              |
|---------------------------|-----------------------|-------------------------|-------------------|
| `dev.mexicox.gob.mx`      | Cloudflare/Google WE1 | Google Trust Services WE1 | GTS Root R4     |
| `cursos.aprende.gob.mx`   | Cloudflare/Google WE1 | Google Trust Services WE1 | GTS Root R4     |

**Observación clave:** ambos dominios comparten **la misma CA
intermediaria y el mismo root**. Un solo par de pines cubre los dos.

**Pin primario (CA intermediaria):**
```
SHA-256 base64: kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=
Sujeto:        Google Trust Services WE1
Vigencia:      2023-12-13  →  2029-02-20 (~3 años)
```

**Pin backup (Root CA):**
```
SHA-256 base64: mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=
Sujeto:        GTS Root R4
Vigencia:      2023-11-15  →  2028-01-28 (~2 años)
```

**No se pinea el cert hoja** porque Cloudflare lo rota cada ~60-90 días
y la app quedaría brickeada.

**Expiration del pin-set: 2028-12-31.** Si Cloudflare cambia de CA antes
de esa fecha (raro), Android desactiva el pinning automáticamente para
no brickear la app — capas 2 y 3 siguen activas como respaldo.

### 4.2 Comportamiento por flavor (capa 3)

| Flavor       | strictMode | Detección | Bloqueo de login |
|--------------|-----------|-----------|------------------|
| `develop`    | false     | ✅ Log    | ❌ No (permite testing en emulador/root) |
| `stage`      | false     | ✅ Log    | ❌ No            |
| `prod`       | **true**  | ✅ Log    | ✅ **Sí**        |

### 4.3 Operación segura del pinning — qué lo rompe y cómo prevenirlo

El SSL pinning es un arma de doble filo: protege contra MITM, pero si los
pines dejan de coincidir con la cadena real, **la app deja de conectar para
TODOS los usuarios** hasta que se publique una nueva versión. Esta sección
documenta exactamente qué cambios son seguros y cuáles son peligrosos, para
evitar dejar la app inservible por accidente.

**Qué se pinea realmente:** no se pinea el certificado del servidor de
origen (Caddy/Let's Encrypt en la MV), sino la **cadena del borde de
Cloudflare** emitida por Google Trust Services:
- Pin primario: CA intermediaria `Google Trust Services WE1` (SPKI).
- Pin backup: raíz `GTS Root R4` (SPKI).

Se pinea el **SPKI** (Subject Public Key Info, la clave pública) y no el
certificado completo, porque el SPKI sobrevive a las renovaciones del cert.
La app acepta la conexión si **cualquiera** de los dos pines coincide.

#### Cambios SEGUROS — NO rompen el pinning ✅

| Cambio | Por qué es seguro |
|--------|-------------------|
| Cloudflare renueva el cert hoja (automático, cada ~60-90 días) | No se pinea la hoja. El SPKI de la intermedia no cambia con la renovación. |
| Se renueva el cert de Caddy/Let's Encrypt en la MV de origen | La app **nunca ve** el cert de origen; ve el de Cloudflare en el borde. La MV es invisible para el pinning. |
| Cambia la config de Caddy, la IP de la MV, el sistema operativo del origen | Mismo motivo: el pinning es sobre el borde de Cloudflare. |
| Google rota la intermediaria WE1 pero la cadena sigue bajo `GTS Root R4` | Entra el pin de respaldo (la raíz), que sigue coincidiendo. |

> **Regla mental:** mientras el dominio siga **fronteado por Cloudflare con
> su cert de Google Trust Services**, todo lo que pase dentro de la máquina
> virtual de origen es irrelevante para el pinning.

#### Cambios PELIGROSOS — rompen el pinning y dejan la app sin conexión 🔴

| Cambio | Efecto | Cómo hacerlo sin romper |
|--------|--------|--------------------------|
| Apagar el proxy de Cloudflare (nube gris / DNS directo a la MV) | La app vería el cert de Let's Encrypt del origen → ningún pin coincide → falla total | No hacerlo sin liberar primero una app con los pines del origen, o sin pinning. |
| Cloudflare cambia su CA emisora (Google → Let's Encrypt / DigiCert) | Ambos pines fallan | Es raro y Cloudflare avisa poco; mitigado por `expiration` del pin-set (ver abajo) y por agregar un 3er pin de respaldo (opcional). |
| Migrar a otro CDN o quitar Cloudflare | Cadena completamente distinta → falla total | Publicar app nueva con la cadena del nuevo proveedor ANTES del cambio en infra. |
| Que Google retire `GTS Root R4` **y** la intermediaria a la vez | Ambos pines fallan | Muy improbable a corto plazo; las raíces duran años. Cubierto por el ciclo de revisión semestral. |

#### Mecanismos de seguridad ya incorporados

1. **Doble pin (intermedia + raíz):** sobrevive a la rotación de la
   intermediaria sin tocar la app.
2. **`expiration="2028-12-31"` en el pin-set (Capa 1 / NSC):** después de
   esa fecha, Android **desactiva el pinning de la NSC automáticamente**
   para no brickear la app. Es una válvula de seguridad. ⚠️ Ojo: la Capa 2
   (OkHttp) **no** tiene expiración, así que un cambio de CA igual la
   rompería — por eso el ciclo de revisión es obligatorio.
3. **Las capas son independientes:** si la NSC se desactiva por expiración,
   las capas 2 y 3 siguen activas.

#### Procedimiento de rotación de pines (sin downtime)

Cuando haya que cambiar de CA / CDN / cert pineado, hacerlo en este orden
para no dejar usuarios fuera:

1. Correr `scripts/extract_ssl_pins.ps1` y obtener los pines nuevos.
2. Publicar una versión de la app que incluya **los pines viejos + los
   nuevos** simultáneamente (en NSC y en `CertificatePinnerProvider`).
3. Esperar a que la base de usuarios actualice (dar margen razonable).
4. Recién entonces hacer el cambio en infraestructura (CA, CDN, etc.).
5. En una versión posterior, retirar los pines viejos.

> **Nunca** cambiar la infraestructura primero y la app después: eso deja
> sin conexión a todos los usuarios de la versión publicada.

#### Checklist preventivo antes de cada release

- [ ] Correr `scripts/extract_ssl_pins.ps1` y confirmar que la intermedia y
      la raíz siguen siendo `Google Trust Services WE1` / `GTS Root R4`.
- [ ] Verificar que la fecha actual está lejos del `expiration` del pin-set.
- [ ] Confirmar que no hay un cambio de infraestructura (Cloudflare/CDN)
      planeado para el corto plazo sin rotación de pines previa.

#### Mejora opcional de robustez

Agregar un **tercer pin de respaldo** de una CA alterna de Cloudflare
reduce el riesgo del escenario "Cloudflare cambia de CA" sin tirar la app.
Tiene un costo: cada pin extra amplía la superficie de certs aceptados, así
que se recomienda solo si se quiere blindar contra ese escenario específico.

---

## 5. Implementación realizada

Rama: `feature/ssl-pinning-defense` (desde `theme-cursos`).

### 5.1 Archivos nuevos

| Archivo | Capa | Propósito |
|---------|------|-----------|
| `app/src/main/res/xml/network_security_config.xml` | 1 | NSC declarativo con pin-set + base-config `cleartextTrafficPermitted=false` |
| `core/src/main/java/org/openedx/core/security/CertificatePinnerProvider.kt` | 2 | Provider único de `okhttp3.CertificatePinner` con los 2 pines |
| `core/src/main/java/org/openedx/core/security/SecurityState.kt` | 3 | `StateFlow` global del estado de compromiso del device |
| `core/src/main/java/org/openedx/core/security/RaspManager.kt` | 3 | Inicialización y callbacks de freeRASP |
| `scripts/extract_ssl_pins.ps1` | Tooling | Regenera los pines SPKI desde los certs vivos (correr cada 3-6 meses) |
| `ssl_pins_output/.gitignore` | Tooling | Excluye los certs descargados del repo |

### 5.2 Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `app/src/main/AndroidManifest.xml` | `android:networkSecurityConfig="@xml/network_security_config"` |
| `app/src/main/java/org/openedx/app/di/NetworkingModule.kt` | `.certificatePinner(CertificatePinnerProvider.build())` en el cliente principal |
| `core/src/main/java/org/openedx/core/module/download/FileDownloader.kt` | Mismo pinning aplicado al cliente de descargas |
| `core/src/main/java/org/openedx/core/module/TranscriptManager.kt` | Mismo pinning aplicado al cliente de subtítulos |
| `app/src/main/java/org/openedx/app/OpenEdXApp.kt` | Llamada `RaspManager.init(...)` en `onCreate` |
| `auth/src/main/java/org/openedx/auth/presentation/signin/SignInViewModel.kt` | Chequeo `blockedByCompromisedDevice()` en `login`, `signInLlaveMx`, `signInLlaveMxWithPKCE`, `signInBrowser`, `signInAuthCode`, `socialAuth` |
| `core/src/main/res/values/strings.xml` | Nuevo string `core_error_compromised_device` |
| `build.gradle` (root) | `freerasp_version = '18.3.0'` |
| `core/build.gradle` | Dependencia `com.aheaditec.talsec.security:TalsecSecurity-Community:$freerasp_version` |
| `settings.gradle` | Repos Maven de Talsec (`europe-west3-maven.pkg.dev`) |

### 5.3 Mensaje al usuario cuando capa 3 bloquea (Opción B)

`core_error_compromised_device`:

> Por seguridad, no se permite iniciar sesión en dispositivos modificados
> (root, debug o emulador). Si crees que esto es un error, contacta a soporte.

---

## 6. Validación

### 6.1 Capa 1 — NSC (✅ Validado)

**Test:** APK debug con pinning instalado + mitmproxy + cert mitmproxy
instalado en el cel.

**Resultado:**
- Login tradicional (`POST /oauth2/access_token`) — falla.
- Login LlaveMX — falla con `Trust anchor for certification path not found`.
- mitmweb **no muestra ningún flow descifrado** a `dev.mexicox.gob.mx`.

Conclusión: el handshake TLS se aborta antes de transmitir cualquier dato.
mitmproxy ve solo intentos TCP iniciales sin handshake completo.

### 6.2 Capa 2 — OkHttp CertificatePinner (✅ Compilada, no llegó a ejecutarse)

Como la capa 1 corta primero, la capa 2 no llegó a evaluar pines. Es el
comportamiento esperado: NSC funciona a nivel del sistema, OkHttp a nivel
de librería. La capa 2 sirve como redundancia si Frida parchea NSC.

### 6.3 Capa 3 — freeRASP (✅ Validado)

**Test:** APK develop debug con freeRASP integrado instalado en el Motorola
G31 (Android 12) con depuración USB y modo desarrollador activos.

**Resultado:**
1. ✅ Inicia sin crash (`Log.i RaspManager: "freeRASP iniciado. strictMode=false packageName=org.openedx.app"`).
2. ✅ Detecta amenazas del entorno de prueba (ADB_ENABLED + DEVELOPER_MODE)
   y las registra vía `SecurityState.markCompromised`.
3. ✅ En `develop` flavor solo loguea, no bloquea el login (correcto por
   diseño — permite QA en emulador/root).
4. ✅ Login real funciona sin proxy: tras quitar el proxy WiFi y el cert de
   mitmproxy, el login se completa contra el cert real de Cloudflare. El
   pinning no interfiere con el tráfico legítimo.
5. ⏭️ En `prod` (o con `strictMode=true` forzado), bloquea login con el
   mensaje `core_error_compromised_device` — pendiente la captura visual
   para la evidencia del oficio.

> Nota sobre tamper: como `signingCertHashesBase64` se pasa vacío, el
> `RaspManager` computa el hash del APK actual para que freeRASP inicialice.
> Esto desactiva *de facto* la detección de tamper/repackaging, pero el
> resto de detecciones (Frida, root, hook, debugger, emulador) — que son
> las que cierran el hallazgo — sí operan. Ver pendiente 8.2.

---

## 7. Riesgos y mitigaciones

| Riesgo | Probabilidad | Mitigación |
|--------|--------------|------------|
| Cloudflare cambia de CA emisora (Google → SSL.com / Let's Encrypt) | Baja-Media | `expiration="2028-12-31"` en el pin-set. Cuando expira, NSC desactiva pinning. Capas 2 y 3 siguen activas. |
| freeRASP genera falsos positivos en usuarios reales con root legítimo | Baja | En `prod` se bloquea login pero no se mata la app. Mensaje claro al usuario. Telemetría a Firebase para medir incidencia. |
| App brickeada por pin incorrecto | Muy baja | Pin primario es CA intermediaria (vigencia 3 años). Pin backup es root (2 años). Doble redundancia. Script `extract_ssl_pins.ps1` para regenerar. |
| Atacante con root logra bypasear las 3 capas | Alta (era ya el caso sin defensa) | El objetivo NO es imposibilidad, es subir costo. Atacante necesita acceso físico al device + horas de trabajo. MASVS-L1 lo considera aceptable para apps no-bancarias. |

---

## 8. Pendientes para cerrar la vulnerabilidad

### 8.1 Para entregar al oficio (deadline 2026-05-30)

- [x] Replicar la vulnerabilidad con mitmproxy y capturar evidencia.
- [x] Diseñar la arquitectura de 3 capas.
- [x] Implementar capa 1 (NSC con pin-set).
- [x] Implementar capa 2 (OkHttp CertificatePinner).
- [x] Implementar capa 3 (freeRASP) — código completo, compila.
- [x] Probar capa 3 en cel (inicia, detecta amenazas, login real funciona).
- [ ] Capturar evidencia visual del bloqueo en `strictMode=true` (Opción B).
- [ ] Probar contra Frida + frida-multiple-unpinning para validar capa 3 (opcional).
- [ ] Liberar APK de release con las 3 capas + firmarlo.
- [ ] Redactar respuesta formal al oficio con evidencia anterior/posterior.
- [ ] Commitear código + documentación en `feature/ssl-pinning-defense`.

### 8.2 Pendientes técnicos a futuro

- [ ] Cuando se libere el primer APK de release con esta defensa,
  extraer el SHA-256 del cert de firma y poblar
  `signingCertHashesBase64` en `OpenEdXApp.initRasp()` para activar
  validación de tamper/repackaging.
- [ ] Cablear telemetría de freeRASP → Firebase Analytics (TODO marcado
  en `RaspManager.report()`).
- [ ] Unificar los 3 `OkHttpClient` en un solo cliente Koin (refactor a
  futuro, fuera de scope de esta vulnerabilidad).
- [ ] Definir cron interno (cada 3-6 meses) para correr
  `scripts/extract_ssl_pins.ps1` y verificar que la cadena de Cloudflare
  no haya cambiado.

### 8.3 Limpieza después de los tests

- [ ] Eliminar cert de mitmproxy del cel.
- [ ] Quitar proxy manual del WiFi del cel.
- [ ] `adb reverse --remove tcp:8080` cuando termines.
- [ ] La rama `security-test/ssl-pinning-replication` se mantiene local
  como respaldo, **NO se pushea a origin**.

---

## 9. Comandos de referencia rápida

### Regenerar pines (cada 3-6 meses)
```powershell
powershell -ExecutionPolicy Bypass -File scripts\extract_ssl_pins.ps1
```

### Build APK debug develop
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDevelopDebug
```

### Build APK release prod
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleProdRelease
```

### Validar con mitmproxy
```powershell
# 1. ADB reverse tunnel
adb reverse tcp:8080 tcp:8080

# 2. Arrancar mitmweb
mitmweb --listen-host 127.0.0.1 --listen-port 8080

# 3. Configurar proxy del cel: 127.0.0.1:8080
# 4. Instalar cert mitmproxy en cel (solo necesario para escenario de evidencia)
# 5. Instalar APK debug y probar login
```

### Logcat filtros útiles
```
package:org.openedx.app tag:RaspManager           # eventos de freeRASP
package:org.openedx.app tag~:OkHttp|SSL|TLS       # tráfico HTTPS
package:org.openedx.app message~:SSLHandshake|trust anchor   # errores de pinning
```

---

## 10. Referencias

- OWASP MASVS 2.0 — Mobile Application Security Verification Standard
- OWASP MASTG — Mobile Application Security Testing Guide
- Android `NetworkSecurityConfig`: <https://developer.android.com/training/articles/security-config>
- OkHttp `CertificatePinner`: <https://square.github.io/okhttp/4.x/okhttp/okhttp3/-certificate-pinner/>
- freeRASP Community: <https://github.com/talsec/Free-RASP-Android>
- Cloudflare Universal SSL CAs: <https://developers.cloudflare.com/ssl/edge-certificates/universal-ssl/>
