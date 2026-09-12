@file:Suppress("DEPRECATION")

package com.asuarez.billetera

import android.app.Activity
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.asuarez.billetera.bundles.BundleActivity
import com.facebook.react.ReactInstanceEventListener
import com.facebook.react.bridge.ReactContext
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val ESPERA_PANTALLA_MS = 20_000L

object EsperaDeBundle {

    fun <A : BundleActivity> contextoListo(escenario: ActivityScenario<A>, segundos: Long = 30) {
        val listo = CountDownLatch(1)
        escenario.onActivity { act ->
            val manager = act.host.reactInstanceManager
            if (manager.currentReactContext != null) {
                listo.countDown()
            } else {
                manager.addReactInstanceEventListener(object : ReactInstanceEventListener {
                    override fun onReactContextInitialized(context: ReactContext) {
                        listo.countDown()
                    }
                })
            }
        }
        assertTrue(
            "El contexto de React no se inicializó en $segundos s",
            listo.await(segundos, TimeUnit.SECONDS)
        )
    }

    // Que exista el contexto no quiere decir que el componente ya haya renderizado, así que
    // además esperamos a que el ReactRootView tenga algo adentro.
    fun <A : BundleActivity> contenidoPintado(escenario: ActivityScenario<A>, segundos: Long = 15) {
        val hijos = AtomicInteger(0)
        val limite = SystemClock.uptimeMillis() + segundos * 1_000
        while (SystemClock.uptimeMillis() < limite && hijos.get() == 0) {
            escenario.onActivity { act ->
                hijos.set(act.reactDelegate?.reactRootView?.childCount ?: 0)
            }
            if (hijos.get() == 0) SystemClock.sleep(200)
        }
        assertTrue("El ReactRootView quedó vacío", hijos.get() > 0)
    }

    // El monitor se registra antes de disparar la navegación: si la Activity alcanza a abrirse
    // primero, la espera se agota sin haberla visto. La que aparezca se cierra aquí para no
    // dejarla encima de la siguiente prueba.
    fun pantallaAbierta(clase: Class<out Activity>, mensaje: String, disparar: () -> Unit = {}) {
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        val vigilante = instrumentacion.addMonitor(clase.name, null, false)
        try {
            disparar()
            val abierta = instrumentacion.waitForMonitorWithTimeout(vigilante, ESPERA_PANTALLA_MS)
            assertNotNull(mensaje, abierta)
            instrumentacion.runOnMainSync { abierta.finish() }
        } finally {
            instrumentacion.removeMonitor(vigilante)
        }
    }
}
