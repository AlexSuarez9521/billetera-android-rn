package com.asuarez.billetera

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.data.Estado
import com.asuarez.billetera.data.Usuario
import com.asuarez.billetera.security.LlaveKeystore
import com.asuarez.billetera.security.SecureStore
import com.asuarez.billetera.session.SesionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SesionPersistenciaTest {

    private lateinit var store: SecureStore

    private val laura = Usuario("u10002", "Laura Pérez", "3109876543", "pbkdf2\$1\$a\$b", Estado.ACTIVO, 480_000)

    @Before
    fun preparar() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = SecureStore(ctx.getSharedPreferences("pruebas.sesion", Context.MODE_PRIVATE), LlaveKeystore::obtener)
        store.clear()
    }

    @Test
    fun otraInstancia_leeLaSesionQueQuedoGuardada() {
        val creada = SesionManager(store, ttlMinutos = 10).crear(laura)

        val recuperada = SesionManager(store, ttlMinutos = 10).actual()

        assertNotNull(recuperada)
        assertEquals(creada.sessionId, recuperada!!.sessionId)
        assertEquals("Laura Pérez", recuperada.name)
        assertEquals("3109876543", recuperada.phone)
    }

    @Test
    fun loGuardadoNoLlevaLaClave() {
        SesionManager(store, ttlMinutos = 10).crear(laura)

        val json = store.get("sesion")
        assertNotNull(json)
        assertFalse(json!!.contains("pbkdf2"))
    }

    @Test
    fun cerrar_borraLaEntrada() {
        val manager = SesionManager(store, ttlMinutos = 10)
        manager.crear(laura)

        manager.cerrar()

        assertNull(store.get("sesion"))
    }
}
