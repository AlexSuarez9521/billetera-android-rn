package com.asuarez.billetera.bundles

import android.os.Bundle
import android.util.Log
import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.nav.Navegacion
import com.asuarez.billetera.security.BridgeCrypto
import org.json.JSONObject

class LoginActivity : BundleActivity() {

    override val bundle: RnBundle
        get() = RnBundle.LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        // Si volvimos aquí por logout o por expiración, la llave de canal anterior no debe seguir
        // sirviendo; hay que rotarla antes de que se armen las props del bundle.
        if (intent?.hasExtra(Navegacion.EXTRA_MOTIVO) == true) {
            BridgeCrypto.rotar()
        }
        super.onCreate(savedInstanceState)
    }

    override fun propsIniciales(): Bundle {
        val props = super.propsIniciales()
        intent?.getStringExtra(Navegacion.EXTRA_MOTIVO)?.let { props.putString("motivo", it) }
        return props
    }

    override fun onBundleEvent(evento: String, payloadJson: String?) {
        if (evento != BridgeEvents.LOGIN_SUCCESS) {
            super.onBundleEvent(evento, payloadJson)
            return
        }
        val anunciada = payloadJson?.let { JSONObject(it).optString("sessionId") }
        if (anunciada != null && anunciada == sesion.actual()?.sessionId) {
            Navegacion.aHome(this)
        } else {
            Log.w("Billetera", "LOGIN_SUCCESS con una sesión que no es la vigente")
        }
    }
}
