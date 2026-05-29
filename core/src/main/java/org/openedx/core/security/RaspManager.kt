package org.openedx.core.security

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo
import com.aheaditec.talsec_security.security.api.Talsec
import com.aheaditec.talsec_security.security.api.TalsecConfig
import com.aheaditec.talsec_security.security.api.ThreatListener
import java.security.MessageDigest

/**
 * Capa 3 de defensa contra el hallazgo TICDEFENSE #2.
 *
 * Inicializa freeRASP (Talsec Community) para detectar entornos
 * comprometidos en tiempo de ejecucion. Cuando se detecta una amenaza,
 * actualiza [SecurityState] que es consultado por los call sites
 * sensibles (SignInViewModel, etc).
 *
 * Comportamiento por flavor:
 *   - prod    -> modo estricto: bloquea login en dispositivos comprometidos
 *   - develop -> modo permisivo: solo registra en logs (permite testing
 *                en emuladores y devices con root)
 *   - stage   -> modo permisivo (igual que develop)
 *
 * El flavor se determina via [strictMode] que el caller debe pasar.
 */
object RaspManager {

    private const val TAG = "RaspManager"
    private const val SECURITY_NOTIFICATION_EMAIL = "soporte@aprende.gob.mx"

    /**
     * Inicializa freeRASP. Debe llamarse desde Application.onCreate.
     *
     * @param context   contexto de la aplicacion
     * @param packageName  package name esperado (hardcoded, no se debe usar
     *        Context.getPackageName porque puede ser manipulado en runtime).
     * @param signingCertHashesBase64  hashes SHA-256 base64 del cert con
     *        el que se firma el APK oficial. Si esta vacio se omite la
     *        validacion de tamper/repackaging (recomendado solo en develop).
     * @param strictMode  si true, [SecurityState.shouldBlockSensitiveOperations]
     *        devolvera true cuando haya amenazas y las operaciones sensibles
     *        se bloquearan. En false solo se registra telemetria.
     */
    fun init(
        context: Context,
        packageName: String,
        signingCertHashesBase64: Array<String>,
        strictMode: Boolean,
    ) {
        SecurityState.strictMode = strictMode

        try {
            // Talsec exige hashes no vacios. Si el caller no los pasa (caso
            // tipico en develop antes de tener el keystore de prod), computamos
            // los del APK actual para que la libreria pueda inicializar. Esto
            // desactiva de facto la deteccion de tamper, pero permite el resto
            // de detecciones (Frida, root, debugger, etc) que son las que
            // realmente cierran el hallazgo TICDEFENSE #2.
            val effectiveHashes = if (signingCertHashesBase64.isNotEmpty()) {
                signingCertHashesBase64
            } else {
                val computed = computeSigningCertificateHashes(context)
                Log.w(TAG, "signingCertHashesBase64 vacio - usando hashes del APK actual: ${computed.toList()}")
                computed
            }

            val config = TalsecConfig.Builder(packageName, effectiveHashes)
                .watcherMail(SECURITY_NOTIFICATION_EMAIL)
                .supportedAlternativeStores(arrayOf())
                .prod(strictMode)
                .build()

            ThreatListener(threatDetected, deviceState).registerListener(context)
            Talsec.start(context, config)

            Log.i(TAG, "freeRASP iniciado. strictMode=$strictMode packageName=$packageName")
        } catch (t: Throwable) {
            // Si freeRASP falla al inicializar, la app DEBE seguir funcionando.
            // Capas 1 (NSC) y 2 (OkHttp CertificatePinner) ya estan activas.
            Log.e(TAG, "freeRASP fallo al inicializar - se continua sin capa 3", t)
        }
    }

    private val threatDetected = object : ThreatListener.ThreatDetected() {
        override fun onRootDetected() = report(SecurityState.Threat.ROOT, "root")
        override fun onDebuggerDetected() = report(SecurityState.Threat.DEBUGGER, "debugger")
        override fun onEmulatorDetected() = report(SecurityState.Threat.EMULATOR, "emulator")
        override fun onTamperDetected() = report(SecurityState.Threat.TAMPERED, "tamper")

        override fun onUntrustedInstallationSourceDetected() =
            report(SecurityState.Threat.UNTRUSTED_INSTALL_SOURCE, "untrusted_install_source")

        override fun onHookDetected() = report(SecurityState.Threat.HOOK, "hook")

        override fun onDeviceBindingDetected() =
            report(SecurityState.Threat.DEVICE_BINDING, "device_binding")

        override fun onObfuscationIssuesDetected() =
            report(SecurityState.Threat.OBFUSCATION, "obfuscation")

        override fun onMalwareDetected(suspiciousApps: List<SuspiciousAppInfo>) {
            report(SecurityState.Threat.MALWARE, "malware:${suspiciousApps.size}")
            suspiciousApps.forEach {
                Log.w(TAG, "Malware app: ${it.packageInfo.packageName}, reasons: ${it.reasons}")
            }
        }

        override fun onAutomationDetected() =
            report(SecurityState.Threat.AUTOMATION, "automation")

        // Las siguientes detecciones NO bloquean login (no comprometen credenciales),
        // solo se registran en log para forensics:
        override fun onScreenshotDetected() {
            Log.w(TAG, "Amenaza informativa: screenshot")
        }

        override fun onScreenRecordingDetected() {
            Log.w(TAG, "Amenaza informativa: screen_recording")
        }

        override fun onMultiInstanceDetected() {
            Log.w(TAG, "Amenaza informativa: multi_instance")
        }

        override fun onUnsecureWifiDetected() {
            Log.w(TAG, "Amenaza informativa: unsecure_wifi")
        }

        override fun onTimeSpoofingDetected() {
            Log.w(TAG, "Amenaza informativa: time_spoofing")
        }

        override fun onLocationSpoofingDetected() {
            Log.w(TAG, "Amenaza informativa: location_spoofing")
        }
    }

    private val deviceState = object : ThreatListener.DeviceState() {
        override fun onSystemVPNDetected() {
            Log.w(TAG, "Estado: system_vpn")
        }

        override fun onDeveloperModeDetected() =
            report(SecurityState.Threat.DEVELOPER_MODE, "developer_mode")

        override fun onADBEnabledDetected() =
            report(SecurityState.Threat.ADB_ENABLED, "adb_enabled")

        override fun onUnlockedDeviceDetected() {
            Log.w(TAG, "Estado: unlocked_device")
        }

        override fun onHardwareBackedKeystoreNotAvailableDetected() {
            Log.w(TAG, "Estado: hw_keystore_unavailable")
        }
    }

    private fun report(threat: SecurityState.Threat, label: String) {
        Log.w(TAG, "Amenaza detectada: $label")
        SecurityState.markCompromised(threat)
        // TODO: enviar evento de telemetria a Firebase Analytics cuando este
        // disponible un AnalyticsProvider desde aqui (sin dependencias circulares).
    }

    /**
     * Calcula el SHA-256 base64 del cert (o certs) que firman el APK actual.
     * Se usa como fallback cuando el caller no pasa hashes explicitos.
     */
    @Suppress("DEPRECATION")
    private fun computeSigningCertificateHashes(context: Context): Array<String> {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, flags)
        val signatures: List<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo
            when {
                signingInfo == null -> emptyList()
                signingInfo.hasMultipleSigners() ->
                    signingInfo.apkContentsSigners?.toList().orEmpty()
                else ->
                    signingInfo.signingCertificateHistory?.toList().orEmpty()
            }
        } else {
            info.signatures?.toList().orEmpty()
        }
        return signatures.map { hashSignature(it) }.toTypedArray()
    }

    private fun hashSignature(signature: Signature): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
