package com.asuarez.billetera

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.bundles.HomeActivity
import com.asuarez.billetera.bundles.MovimientosActivity
import com.asuarez.billetera.bundles.TransferenciaActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavegacionDesdeHomeTest {

    private lateinit var app: BilleteraApp

    @Before
    fun abrirSesion() {
        app = BilleteraApp.de(ApplicationProvider.getApplicationContext<Context>())
        app.sesion.crear(app.banco.buscarPorCelular("3001234567")!!)
    }

    @After
    fun cerrarSesion() {
        app.sesion.cerrar()
    }

    @Test
    fun openTransferAbreLaPantallaDeTransferencia() {
        homeRecibe(BridgeEvents.OPEN_TRANSFER, TransferenciaActivity::class.java)
    }

    @Test
    fun openMovementsAbreLaPantallaDeMovimientos() {
        homeRecibe(BridgeEvents.OPEN_MOVEMENTS, MovimientosActivity::class.java)
    }

    private fun homeRecibe(evento: String, destino: Class<out Activity>) {
        ActivityScenario.launch(HomeActivity::class.java).use { escenario ->
            EsperaDeBundle.pantallaAbierta(destino, "$evento no abrió ${destino.simpleName}") {
                escenario.onActivity { act -> act.onBundleEvent(evento, null) }
            }
        }
    }
}
