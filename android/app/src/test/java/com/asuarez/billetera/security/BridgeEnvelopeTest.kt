package com.asuarez.billetera.security

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

class BridgeEnvelopeTest {

    private val vector = JSONObject(archivoDelVector().readText(Charsets.UTF_8))
    private val llaveDePrueba = SecretKeySpec(deB64(vector.getString("clavePruebaB64")), "AES")

    @Test
    fun `descifra el envelope que generó el lado de JS`() {
        val sobre = JSONObject(vector.getString("envelope"))

        val plano = AesGcm.descifrar(llaveDePrueba, deB64(sobre.getString("iv")), deB64(sobre.getString("data")))

        assertEquals(vector.getString("textoPlano"), String(plano, Charsets.UTF_8))
    }

    @Test
    fun `con el mismo iv produce exactamente el mismo data`() {
        val iv = deB64(vector.getString("ivPruebaB64"))
        val plano = vector.getString("textoPlano").toByteArray(Charsets.UTF_8)

        val ct = AesGcm.cifrarConIv(llaveDePrueba, iv, plano)

        assertEquals(
            JSONObject(vector.getString("envelope")).getString("data"),
            Base64.getEncoder().encodeToString(ct)
        )
    }

    @Test
    fun `el envelope de salida trae iv de 12 bytes y data en base64`() {
        BridgeCrypto.inicializar()
        val payload = """{"celular":"3001234567","clave":"Clave123"}"""

        val sobre = JSONObject(BridgeCrypto.cifrar(payload))

        assertEquals(12, deB64(sobre.getString("iv")).size)
        assertTrue(sobre.getString("data").isNotEmpty())
        assertEquals(payload, BridgeCrypto.descifrar(sobre.toString()))
    }

    @Test(expected = PayloadInvalido::class)
    fun `un data alterado se rechaza`() {
        BridgeCrypto.inicializar()
        val sobre = JSONObject(BridgeCrypto.cifrar("""{"monto":1000}"""))
        val data = deB64(sobre.getString("data"))
        data[0] = (data[0] + 7).toByte()

        BridgeCrypto.descifrar(sobre.put("data", Base64.getEncoder().encodeToString(data)).toString())
    }

    @Test(expected = PayloadInvalido::class)
    fun `un envelope sin data se rechaza`() {
        BridgeCrypto.inicializar()

        BridgeCrypto.descifrar("""{"iv":"H6IMk153CLTGLeE5"}""")
    }

    @Test
    fun `al rotar la llave cambia la que se le pasa al bundle`() {
        BridgeCrypto.inicializar()
        val anterior = BridgeCrypto.claveBase64()

        BridgeCrypto.rotar()

        assertNotEquals(anterior, BridgeCrypto.claveBase64())
        assertEquals(32, deB64(BridgeCrypto.claveBase64()).size)
    }

    private fun deB64(texto: String): ByteArray = Base64.getDecoder().decode(texto)

    private fun archivoDelVector(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            val candidato = File(dir, "test-vectors/aes-gcm.json")
            if (candidato.isFile) return candidato
            dir = dir.parentFile
        }
        throw IllegalStateException("No encontré test-vectors/aes-gcm.json desde ${System.getProperty("user.dir")}")
    }
}
