package com.asuarez.billetera.bundles

import android.util.Log
import com.asuarez.billetera.bridge.BridgeEvents
import org.json.JSONObject

class TransferenciaActivity : BundleActivity() {

    override val bundle: RnBundle
        get() = RnBundle.TRANSFERENCIA

    override fun onBundleEvent(evento: String, payloadJson: String?) {
        if (evento == BridgeEvents.TRANSFER_SUCCESS) {
            // La pantalla la cierra el mismo bundle con el back; Home se refresca al reanudar.
            val id = payloadJson?.let { JSONObject(it).optString("movimientoId") }
            Log.i("Billetera", "Transferencia registrada: $id")
        } else {
            super.onBundleEvent(evento, payloadJson)
        }
    }
}
