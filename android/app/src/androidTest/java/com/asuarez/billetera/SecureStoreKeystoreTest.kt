package com.asuarez.billetera

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asuarez.billetera.security.LlaveKeystore
import com.asuarez.billetera.security.SecureStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureStoreKeystoreTest {

    private val prefs = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("pruebas.securestore", Context.MODE_PRIVATE)

    private val store = SecureStore(prefs, LlaveKeystore::obtener)

    @After
    fun limpiar() {
        store.clear()
    }

    @Test
    fun guardaYRecuperaConLaLlaveDelKeystore() {
        store.put("sesion", "{\"sessionId\":\"abc-123\"}")

        assertEquals("{\"sessionId\":\"abc-123\"}", store.get("sesion"))
    }

    @Test
    fun elValorCrudoNoEsElTextoPlano() {
        store.put("sesion", "3001234567")

        val crudo = prefs.getString("sesion", null)
        assertNotNull(crudo)
        assertFalse(crudo!!.contains("3001234567"))
    }

    @Test
    fun unValorManipuladoDevuelveNull() {
        store.put("sesion", "algo que importa")
        prefs.edit().putString("sesion", "AAAAAAAAAAAAAAAAAAAAAA==.QUJDRA==").commit()

        assertNull(store.get("sesion"))
        assertNull(prefs.getString("sesion", null))
    }
}
