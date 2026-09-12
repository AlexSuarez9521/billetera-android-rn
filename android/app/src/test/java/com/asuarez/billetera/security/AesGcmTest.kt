package com.asuarez.billetera.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.spec.SecretKeySpec

class AesGcmTest {

    private val llave = SecretKeySpec(ByteArray(32).also { SecureRandom().nextBytes(it) }, "AES")

    @Test
    fun `cifra y descifra el mismo contenido`() {
        val plano = """{"celular":"3001234567","monto":125000}""".toByteArray(Charsets.UTF_8)

        val (iv, ct) = AesGcm.cifrar(llave, plano)

        assertEquals(12, iv.size)
        assertEquals(plano.size + 16, ct.size)
        assertArrayEquals(plano, AesGcm.descifrar(llave, iv, ct))
    }

    @Test(expected = AEADBadTagException::class)
    fun `un ciphertext manipulado no abre`() {
        val (iv, ct) = AesGcm.cifrar(llave, "saldo disponible".toByteArray())
        ct[ct.size - 1] = (ct[ct.size - 1] + 1).toByte()

        AesGcm.descifrar(llave, iv, ct)
    }

    @Test
    fun `dos mensajes iguales salen distintos`() {
        val (iv1, ct1) = AesGcm.cifrar(llave, "mismo texto".toByteArray())
        val (iv2, ct2) = AesGcm.cifrar(llave, "mismo texto".toByteArray())

        assertFalse(iv1.contentEquals(iv2))
        assertFalse(ct1.contentEquals(ct2))
    }

    @Test
    fun `varias llamadas seguidas no reusan el iv`() {
        val mensajes = (1..20).map { "movimiento $it" }
        val sobres = mensajes.map { AesGcm.cifrar(llave, it.toByteArray(Charsets.UTF_8)) }

        mensajes.forEachIndexed { i, mensaje ->
            val (iv, ct) = sobres[i]
            assertEquals(mensaje, String(AesGcm.descifrar(llave, iv, ct), Charsets.UTF_8))
        }
        val ivs = sobres.map { Base64.getEncoder().encodeToString(it.first) }.toSet()
        assertEquals(mensajes.size, ivs.size)
    }

    @Test
    fun `descifra lo cifrado con un iv propio`() {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }

        val ct = AesGcm.cifrarConIv(llave, iv, "vector".toByteArray())

        assertEquals("vector", String(AesGcm.descifrar(llave, iv, ct), Charsets.UTF_8))
    }
}
