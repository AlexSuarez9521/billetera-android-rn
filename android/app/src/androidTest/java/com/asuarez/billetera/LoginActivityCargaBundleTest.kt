package com.asuarez.billetera

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bundles.LoginActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityCargaBundleTest {

    @Before
    fun sinSesionPrevia() {
        BilleteraApp.de(ApplicationProvider.getApplicationContext<Context>()).sesion.cerrar()
    }

    @Test
    fun montaElBundleDeLogin() {
        ActivityScenario.launch(LoginActivity::class.java).use { escenario ->
            EsperaDeBundle.contextoListo(escenario)
            EsperaDeBundle.contenidoPintado(escenario)

            assertEquals(Lifecycle.State.RESUMED, escenario.state)
            escenario.onActivity { act ->
                assertTrue(act.host.hasInstance())
                assertTrue(act.propsIniciales().getString("claveCanal")!!.isNotEmpty())
            }
        }
    }
}
