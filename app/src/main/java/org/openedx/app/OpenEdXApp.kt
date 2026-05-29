package org.openedx.app

import android.app.Application
import com.braze.Braze
import com.braze.configuration.BrazeConfig
import com.braze.ui.BrazeDeeplinkHandler
import com.google.firebase.FirebaseApp
import io.branch.referral.Branch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.openedx.app.deeplink.BranchBrazeDeeplinkHandler
import org.openedx.app.di.appModule
import org.openedx.app.di.networkingModule
import org.openedx.app.di.screenModule
import org.openedx.core.config.Config
import org.openedx.core.security.RaspManager
import org.openedx.firebase.OEXFirebaseAnalytics

class OpenEdXApp : Application() {

    private val config by inject<Config>()
    private val pluginManager by inject<PluginManager>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenEdXApp)
            modules(
                appModule,
                networkingModule,
                screenModule
            )
        }

        initRasp()

        if (config.getFirebaseConfig().enabled) {
            FirebaseApp.initializeApp(this)
        }

        if (config.getBranchConfig().enabled) {
            if (BuildConfig.DEBUG) {
                Branch.enableTestMode()
                Branch.enableLogging()
            }
            Branch.expectDelayedSessionInitialization(true)
            Branch.getAutoInstance(this)
        }

        if (config.getBrazeConfig().isEnabled && config.getFirebaseConfig().enabled) {
            val isCloudMessagingEnabled = config.getFirebaseConfig().isCloudMessagingEnabled &&
                    config.getBrazeConfig().isPushNotificationsEnabled

            val brazeConfig = BrazeConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(isCloudMessagingEnabled)
                .setFirebaseCloudMessagingSenderIdKey(config.getFirebaseConfig().projectNumber)
                .setHandlePushDeepLinksAutomatically(true)
                .setIsFirebaseMessagingServiceOnNewTokenRegistrationEnabled(true)
                .build()
            Braze.configure(this, brazeConfig)

            if (config.getBranchConfig().enabled) {
                BrazeDeeplinkHandler.setBrazeDeeplinkHandler(BranchBrazeDeeplinkHandler())
            }
        }

        initPlugins()
    }

    private fun initPlugins() {
        if (config.getFirebaseConfig().enabled) {
            pluginManager.addPlugin(OEXFirebaseAnalytics(context = this))
        }
    }

    /**
     * Capa 3 de defensa contra el hallazgo TICDEFENSE #2 (Bypass SSL Pinning).
     *
     * En el flavor `prod` activa modo estricto: si freeRASP detecta Frida,
     * root, hook, etc., se bloquea el login en SignInViewModel.
     *
     * En develop/stage solo se registra en logs para no entorpecer el
     * trabajo del equipo (emulador, devices con root para QA, etc).
     */
    private fun initRasp() {
        val strictMode = BuildConfig.FLAVOR == FLAVOR_PROD

        // Talsec recomienda hardcodear el package name (no usar Context.getPackageName)
        // porque puede ser manipulado en runtime por un atacante.
        val expectedPackage = if (strictMode) PROD_PACKAGE_NAME else DEV_PACKAGE_NAME

        // expectedSigningCertificateHashesBase64: se deja vacio mientras no
        // tengamos el hash SHA-256 del keystore de release. Hasta entonces
        // freeRASP no validara tamper/repackaging, pero si detecta el resto
        // de amenazas (Frida, root, debugger, emulator, hooking, etc).
        RaspManager.init(
            context = this,
            packageName = expectedPackage,
            signingCertHashesBase64 = emptyArray(),
            strictMode = strictMode,
        )
    }

    companion object {
        private const val FLAVOR_PROD = "prod"
        // Package names del YAML config: develop=org.openedx.app, prod=mx.aprende.android
        private const val DEV_PACKAGE_NAME = "org.openedx.app"
        private const val PROD_PACKAGE_NAME = "mx.aprende.android"
    }
}
