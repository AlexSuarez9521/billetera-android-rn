package com.asuarez.billetera

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.bundles.HomeActivity
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeActivityCargaBundleTest {

    private lateinit var app: BilleteraApp

    @Before
    fun abrirSesion() {
        app = BilleteraApp.de(ApplicationProvider.getApplicationContext<Context>())
        val alex = app.banco.buscarPorCelular("3001234567")
        assertNotNull("Falta el usuario semilla", alex)
        app.sesion.crear(alex!!)
    }

    @After
    fun cerrarSesion() {
        app.sesion.cerrar()
    }

    @Test
    fun conSesionValidaMontaHomeYAceptaElHandshake() {
        ActivityScenario.launch(HomeActivity::class.java).use { escenario ->
            EsperaDeBundle.contextoListo(escenario)
            EsperaDeBundle.contenidoPintado(escenario)

            escenario.onActivity { act -> act.onBundleEvent(BridgeEvents.HOME_READY, null) }
            assertTrue(app.sesion.esValida())
        }
    }
}
