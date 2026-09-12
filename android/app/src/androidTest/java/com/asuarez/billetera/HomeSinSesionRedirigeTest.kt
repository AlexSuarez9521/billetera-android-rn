package com.asuarez.billetera

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.bundles.HomeActivity
import com.asuarez.billetera.bundles.LoginActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeSinSesionRedirigeTest {

    // Home se cierra dentro de onCreate, así que nunca llega a RESUMED y ActivityScenario se
    // quedaría esperando; aquí basta con lanzarla a mano y ver dónde termina el usuario.
    @Test
    fun sinSesionNoAbreHome() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        BilleteraApp.de(ctx).sesion.cerrar()

        EsperaDeBundle.pantallaAbierta(LoginActivity::class.java, "Home debía mandar a Login") {
            ctx.startActivity(
                Intent(ctx, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
