package com.asuarez.billetera.bundles

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.asuarez.billetera.BilleteraApp
import com.asuarez.billetera.BuildConfig
import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.bridge.JsEmitter
import com.asuarez.billetera.nav.Navegacion
import com.asuarez.billetera.security.BridgeCrypto
import com.asuarez.billetera.session.SesionManager
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.UiThreadUtil
import org.json.JSONObject

private const val TAG = "Billetera"

/**
 * Base de las cuatro pantallas de React Native. Cada una levanta su propio [BundleHost] y lo
 * destruye en onDestroy, y recibe por [onBundleEvent] los eventos que manda su bundle, ya
 * descifrados y siempre en el hilo principal.
 */
abstract class BundleActivity : ReactActivity() {

    abstract val bundle: RnBundle

    val host: BundleHost by lazy { BundleHost(application, bundle) { fallo(it) } }

    internal var sesionCaida = false
        private set

    protected val app: BilleteraApp
        get() = application as BilleteraApp

    protected val sesion: SesionManager
        get() = app.sesion

    private val principal = Handler(Looper.getMainLooper())

    private var expirando = false

    private val alExpirar = Runnable { sesionExpiro() }

    private val aLoginPorExpiracion = Runnable {
        Navegacion.aLogin(this, "expirada")
        finish()
    }

    override fun getMainComponentName(): String = bundle.componente

    override fun createReactActivityDelegate(): ReactActivityDelegate = BundleDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        sesionCaida = bundle.privado && !sesion.esValida()
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        // Con sesionCaida el delegate le niega el componente y super.onCreate no llega a cargar el
        // bundle; saltarse el super, en cambio, deja el ReactDelegate en null y revienta al cerrar.
        super.onCreate(savedInstanceState)
        if (sesionCaida) {
            Navegacion.aLogin(this, "expirada")
            finish()
            return
        }
        separarDeLasBarras()
    }

    // Desde targetSdk 35 la ventana va siempre de borde a borde. El padding no puede ir en el
    // ReactRootView porque en arquitectura clásica el UIManager coloca los hijos por su cuenta y
    // se lo salta; el contenedor de la Activity sí lo respeta.
    private fun separarDeLasBarras() {
        val contenedor = findViewById<View>(android.R.id.content) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(contenedor) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(0, barras.top, 0, barras.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bundle.privado || isFinishing) return

        val s = sesion.actual()
        if (s == null) {
            Navegacion.aLogin(this, "expirada")
            finish()
            return
        }
        principal.postDelayed(alExpirar, (s.expiraEnMs - System.currentTimeMillis()).coerceAtLeast(0))
    }

    override fun onPause() {
        super.onPause()
        principal.removeCallbacks(alExpirar)
        // Si se va la pantalla con la cuenta regresiva andando, el startActivity no sale desde
        // segundo plano pero el finish() sí, y la tarea queda vacía.
        principal.removeCallbacks(aLoginPorExpiracion)
    }

    override fun onDestroy() {
        super.onDestroy()
        principal.removeCallbacksAndMessages(null)
        host.clear()
    }

    /**
     * Único punto que avisa de la sesión caída, lo llame el Handler o el puente al rechazar una
     * operación: si se dejaran las dos rutas sueltas, Login se crearía dos veces seguidas.
     */
    fun sesionExpiro() {
        if (expirando) return
        expirando = true
        sesion.cerrar()
        emitir(BridgeEvents.SESSION_EXPIRED, JSONObject().put("motivo", "EXPIRADA").toString())
        // Los dos segundos le dan al bundle tiempo de pintar el aviso antes de que la pantalla
        // desaparezca.
        principal.postDelayed(aLoginPorExpiracion, 2_000)
    }

    open fun onBundleEvent(evento: String, payloadJson: String?) {
        Log.w(TAG, "Evento $evento sin atender en ${bundle.nombre}")
    }

    fun emitir(evento: String, payloadJson: String? = null) {
        JsEmitter.emitir(host, evento, payloadJson)
    }

    open fun propsIniciales(): Bundle = Bundle().apply {
        putString("claveCanal", BridgeCrypto.claveBase64())
        putString("bundle", bundle.nombre)
        putBoolean("esDebug", BuildConfig.DEBUG)
        putString("version", BuildConfig.VERSION_NAME)
    }

    private fun fallo(e: Throwable) {
        Log.e(TAG, "Fallo en bundle ${bundle.nombre}", e)
        UiThreadUtil.runOnUiThread(Runnable {
            if (isFinishing || isDestroyed) return@Runnable
            Navegacion.aError(this)
            finish()
        })
    }
}
