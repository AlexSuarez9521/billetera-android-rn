package com.asuarez.billetera

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bridge.BridgeEvents
import com.asuarez.billetera.bundles.HomeActivity
import com.asuarez.billetera.bundles.LoginActivity
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutLimpiaSesionTest {

    @Test
    fun logoutBorraLaSesionYVuelveALogin() {
        val app = BilleteraApp.de(ApplicationProvider.getApplicationContext<Context>())
        app.sesion.crear(app.banco.buscarPorCelular("3001234567")!!)

        ActivityScenario.launch(HomeActivity::class.java).use { escenario ->
            EsperaDeBundle.pantallaAbierta(LoginActivity::class.java, "Después de LOGOUT no apareció Login") {
                escenario.onActivity { act -> act.onBundleEvent(BridgeEvents.LOGOUT, null) }
            }
            assertNull(app.sesion.actual())
        }
    }
}
