package org.openedx.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado global del nivel de compromiso del dispositivo, alimentado por freeRASP.
 *
 * Forma parte de la capa 3 de defensa contra el hallazgo TICDEFENSE #2.
 * Cuando freeRASP detecta una amenaza (Frida, root, hooking, debugger,
 * emulator, repackaging), llama a [markCompromised] con el tipo de amenaza.
 *
 * Los puntos sensibles (login, refresh de token, descarga de contenido)
 * consultan [isCompromised] para decidir si bloquear la operacion. El uso
 * concreto se hace en SignInViewModel y otros consumidores.
 *
 * En flavors no productivos (develop, stage) [strictMode] es false y la
 * deteccion solo se registra. En `prod` el modo estricto bloquea las
 * operaciones sensibles.
 */
object SecurityState {

    data class CompromiseReport(
        val threats: Set<Threat> = emptySet(),
    ) {
        val isCompromised: Boolean
            get() = threats.isNotEmpty()
    }

    /**
     * Cada amenaza declara si debe BLOQUEAR operaciones sensibles o si es
     * solo telemetría. La distinción es clave para la distribución por APK
     * directo: amenazas como [UNTRUSTED_INSTALL_SOURCE] se disparan en CUALQUIER
     * instalación fuera de Play Store (o sea, todos los usuarios legítimos),
     * así que NO deben bloquear; solo las que indican un bypass real lo hacen.
     */
    enum class Threat(val blocksSensitiveOperations: Boolean) {
        // Indican un intento real de bypass / entorno comprometido -> bloquean
        ROOT(true),
        DEBUGGER(true),
        EMULATOR(true),
        HOOK(true),
        TAMPERED(true),
        MALWARE(true),
        AUTOMATION(true),

        // Ruido esperado en distribución por APK directo / power users -> solo telemetría
        UNTRUSTED_INSTALL_SOURCE(false),
        DEVICE_BINDING(false),
        OBFUSCATION(false),
        DEVELOPER_MODE(false),
        ADB_ENABLED(false),
    }

    private val _state = MutableStateFlow(CompromiseReport())
    val state: StateFlow<CompromiseReport> = _state.asStateFlow()

    /**
     * True si el modo estricto esta activo (bloquea operaciones sensibles
     * cuando hay amenazas). Lo configura [RaspManager] segun el flavor.
     */
    @Volatile
    var strictMode: Boolean = false
        internal set

    /**
     * True si hay alguna amenaza BLOQUEANTE detectada Y el modo estricto esta
     * activo. Las amenazas de solo-telemetria (install source, developer mode,
     * adb, etc.) NO cuentan aqui para no bloquear usuarios legitimos.
     * Los call sites deben consultar esto antes de operaciones sensibles.
     */
    val shouldBlockSensitiveOperations: Boolean
        get() = strictMode && _state.value.threats.any { it.blocksSensitiveOperations }

    /**
     * Registra una amenaza detectada por freeRASP. Idempotente.
     */
    internal fun markCompromised(threat: Threat) {
        _state.update { current ->
            if (threat in current.threats) current
            else current.copy(threats = current.threats + threat)
        }
    }

    /**
     * Limpia el estado. Solo se usa para tests.
     */
    internal fun reset() {
        _state.update { CompromiseReport() }
    }
}
