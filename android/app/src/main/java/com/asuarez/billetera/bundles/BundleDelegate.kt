@file:Suppress("DEPRECATION")

package com.asuarez.billetera.bundles

import android.os.Bundle
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost

class BundleDelegate(private val act: BundleActivity) :
    ReactActivityDelegate(act, act.bundle.componente) {

    // Sin nombre de componente el delegate arma todo menos el surface. Es la forma de que
    // super.onCreate no cargue el bundle cuando la sesión ya venció y solo vamos a redirigir.
    override fun getMainComponentName(): String? =
        if (act.sesionCaida) null else act.bundle.componente

    override fun getReactNativeHost(): ReactNativeHost = act.host

    // Sin bridgeless no hay ReactHost, y el de la clase base castea la Application a ReactApplication.
    override fun getReactHost(): ReactHost? = null

    override fun getLaunchOptions(): Bundle = act.propsIniciales()
}
