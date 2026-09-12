package com.asuarez.billetera.session

import com.asuarez.billetera.data.Estado
import com.asuarez.billetera.data.Usuario
import com.asuarez.billetera.util.StoreEnMemoria
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SesionManagerTest {

    private val alex = Usuario(
        "u10001",
        "Alex Suárez",
        "3001234567",
        "pbkdf2\$210000\$c2FsZGVtZW50aXJhcw==\$aGFzaHF1ZW5vc2V1c2FlbmVzdGF0ZXN0",
        Estado.ACTIVO,
        1_250_000L
    )

    private lateinit var store: StoreEnMemoria
    private lateinit var sesiones: SesionManager
    private var ahora = 1_757_600_000_000L

    @Before
    fun preparar() {
        store = StoreEnMemoria()
        sesiones = SesionManager(store, { ahora }, ttlMinutos = 2)
    }

    @Test
    fun `crear deja la sesión guardada y vigente`() {
        val creada = sesiones.crear(alex)

        assertEquals("u10001", creada.userId)
        assertEquals(ahora + 120_000, creada.expiraEnMs)
        assertTrue(sesiones.esValida())
        assertEquals(creada.sessionId, sesiones.actual()?.sessionId)
    }

    @Test
    fun `dos sesiones seguidas no comparten el id`() {
        val primera = sesiones.crear(alex)
        val segunda = sesiones.crear(alex)

        assertEquals(36, primera.sessionId.length)
        assertNotEquals(primera.sessionId, segunda.sessionId)
    }

    @Test
    fun `cuando pasa el ttl la sesión deja de valer y se borra`() {
        sesiones.crear(alex)

        ahora += 120_000

        assertNull(sesiones.actual())
        assertFalse(sesiones.esValida())
        assertTrue(store.vacio())
    }

    @Test
    fun `cerrar borra lo que había guardado`() {
        sesiones.crear(alex)

        sesiones.cerrar()

        assertNull(sesiones.actual())
        assertTrue(store.vacio())
    }

    @Test
    fun `el json guardado trae los datos de la sesión y nada de la clave`() {
        val creada = sesiones.crear(alex)
        val json = JSONObject(store.get("sesion")!!)

        assertEquals(creada.sessionId, json.getString("sessionId"))
        assertEquals("Alex Suárez", json.getString("name"))
        assertEquals("3001234567", json.getString("phone"))
        assertEquals(creada.expiresAt, json.getString("expiresAt"))
        assertEquals(ahora + 120_000, json.getLong("expiraEnMs"))
        assertFalse(json.has("clave"))
        assertFalse(json.has("claveHash"))
        assertFalse(store.get("sesion")!!.contains("pbkdf2"))
    }

    @Test
    fun `un valor corrupto se descarta en vez de reventar`() {
        store.put("sesion", "esto no es json")

        assertNull(sesiones.actual())
        assertTrue(store.vacio())
    }
}
