package com.asuarez.billetera.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.asuarez.billetera.BilleteraApp
import com.asuarez.billetera.BuildConfig
import com.asuarez.billetera.R
import com.asuarez.billetera.nav.Navegacion
import com.asuarez.billetera.security.DeviceIntegrity
import com.asuarez.billetera.security.Diagnostico

private const val MINIMO_EN_PANTALLA = 1_200L

class SplashActivity : AppCompatActivity() {

    private val principal = Handler(Looper.getMainLooper())
    private val arranque = SystemClock.elapsedRealtime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // El diagnóstico lee archivos y lanza getprop, así que va fuera del hilo de UI; puede
        // terminar con la pantalla ya cerrada y por eso trabaja sobre el contexto de la aplicación.
        val contexto = applicationContext
        Thread {
            val d = DeviceIntegrity.diagnosticar(contexto)
            val falta = MINIMO_EN_PANTALLA - (SystemClock.elapsedRealtime() - arranque)
            principal.postDelayed({ decidir(d) }, falta.coerceAtLeast(0))
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        principal.removeCallbacksAndMessages(null)
    }

    private fun decidir(d: Diagnostico) {
        if (isFinishing || isDestroyed) return

        val bloquear = d.rooteado && !d.emulador && !BuildConfig.DEBUG
        when {
            bloquear -> bloqueo(d)
            d.senales.isEmpty() || DeviceIntegrity.omitirAvisoEnPruebas -> seguir()
            else -> advertencia(d)
        }
    }

    private fun bloqueo(d: Diagnostico) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dispositivo_no_seguro)
            .setMessage(getString(R.string.dispositivo_no_seguro_detalle, d.senales.joinToString("\n")))
            .setCancelable(false)
            .setPositiveButton(R.string.salir) { _, _ -> finish() }
            .show()
    }

    private fun advertencia(d: Diagnostico) {
        val dialogo = AlertDialog.Builder(this)
            .setTitle(R.string.revisa_el_dispositivo)
            .setMessage(getString(R.string.senales_detectadas, d.senales.joinToString("\n")))
            .setCancelable(false)
            .setPositiveButton(R.string.continuar) { _, _ -> seguir() }
        if (BuildConfig.DEBUG) {
            dialogo.setNegativeButton(R.string.salir) { _, _ -> finish() }
        }
        dialogo.show()
    }

    private fun seguir() {
        val app = BilleteraApp.de(this)
        if (app.sesion.esValida()) Navegacion.aHome(this) else Navegacion.aLogin(this)
        finish()
    }
}
