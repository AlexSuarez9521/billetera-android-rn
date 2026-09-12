@file:Suppress("DEPRECATION")

package com.asuarez.billetera.bridge

import android.util.Log
import com.asuarez.billetera.bundles.BundleHost
import com.asuarez.billetera.security.BridgeCrypto

object JsEmitter {

    fun emitir(host: BundleHost, evento: String, payloadJson: String?) {
        if (!host.hasInstance()) return

        val ctx = host.reactInstanceManager.currentReactContext
        if (ctx == null) {
            Log.w("Billetera", "Todavía no hay contexto de React para $evento")
            return
        }
        if (!ctx.hasActiveReactInstance()) return

        ctx.emitDeviceEvent(evento, payloadJson?.let { BridgeCrypto.cifrar(it) })
    }
}
