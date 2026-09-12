package com.asuarez.billetera

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bundles.LoginActivity
import com.asuarez.billetera.security.DeviceIntegrity
import com.asuarez.billetera.splash.SplashActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashRedirigeALoginTest {

    @Before
    fun preparar() {
        DeviceIntegrity.omitirAvisoEnPruebas = true
        BilleteraApp.de(ApplicationProvider.getApplicationContext<Context>()).sesion.cerrar()
    }

    @After
    fun restaurar() {
        DeviceIntegrity.omitirAvisoEnPruebas = false
    }

    @Test
    fun sinSesionTerminaEnLogin() {
        ActivityScenario.launch(SplashActivity::class.java).use {
            EsperaDeBundle.pantallaAbierta(LoginActivity::class.java, "El splash no llevó a Login")
        }
    }
}
