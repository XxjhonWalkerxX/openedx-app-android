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

    enum class Threat {
        ROOT,
        DEBUGGER,
        EMULATOR,
        HOOK,
        TAMPERED,
        UNTRUSTED_INSTALL_SOURCE,
        DEVICE_BINDING,
        OBFUSCATION,
        MALWARE,
        AUTOMATION,
        DEVELOPER_MODE,
        ADB_ENABLED,
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
     * True si hay alguna amenaza detectada Y el modo estricto esta activo.
     * Los call sites deben consultar esto antes de operaciones sensibles.
     */
    val shouldBlockSensitiveOperations: Boolean
        get() = strictMode && _state.value.isCompromised

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
