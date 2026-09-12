package com.asuarez.billetera.bundles

import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.nav.Navegacion
import org.json.JSONObject

class HomeActivity : BundleActivity() {

    override val bundle: RnBundle
        get() = RnBundle.HOME

    private var homeListo = false

    // Levantar la pantalla hija toma varios cientos de ms y en ese rato Home sigue recibiendo
    // toques; sin esto, dos toques apilan dos Activities o rotan la llave de canal dos veces.
    private var navegando = false

    override fun onBundleEvent(evento: String, payloadJson: String?) {
        when (evento) {
            BridgeEvents.HOME_READY -> {
                homeListo = true
                enviarHome()
            }
            BridgeEvents.OPEN_TRANSFER -> abrir { Navegacion.aTransferencia(this) }
            BridgeEvents.OPEN_MOVEMENTS -> abrir { Navegacion.aMovimientos(this) }
            BridgeEvents.LOGOUT -> abrir {
                sesion.cerrar()
                Navegacion.aLogin(this, "logout")
                finish()
            }
            else -> super.onBundleEvent(evento, payloadJson)
        }
    }

    override fun onResume() {
        super.onResume()
        navegando = false
        // Al volver de una transferencia el saldo cambió y el bundle sigue montado con los datos
        // viejos; se los reenviamos en vez de recargar la pantalla entera.
        if (homeListo) enviarHome()
    }

    private fun abrir(ir: () -> Unit) {
        if (navegando) return
        navegando = true
        if (sesion.esValida()) ir() else sesionExpiro()
    }

    private fun enviarHome() {
        val s = sesion.actual() ?: return
        val u = app.banco.usuario(s.userId) ?: return
        val datos = JSONObject()
            .put("nombre", s.name)
            .put("celular", s.phone)
            .put("saldo", u.saldo)
            .put("expiraEn", s.expiresAt)
        emitir(BridgeEvents.LOAD_HOME, datos.toString())
    }
}
