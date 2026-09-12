package com.asuarez.billetera.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Base64

class PasswordHasherTest {

    @Test
    fun `verifica la clave con la que se generó`() {
        val guardado = PasswordHasher.hash("Clave123")

        assertTrue(PasswordHasher.verificar("Clave123", guardado))
    }

    @Test
    fun `rechaza otra clave y también la diferencia de mayúsculas`() {
        val guardado = PasswordHasher.hash("Clave123")

        assertFalse(PasswordHasher.verificar("Clave124", guardado))
        assertFalse(PasswordHasher.verificar("clave123", guardado))
        assertFalse(PasswordHasher.verificar("", guardado))
    }

    @Test
    fun `el formato guarda iteraciones, salt y hash`() {
        val partes = PasswordHasher.hash("Clave123").split("\$")

        assertEquals(4, partes.size)
        assertEquals("pbkdf2", partes[0])
        assertEquals("210000", partes[1])
        assertEquals(16, Base64.getDecoder().decode(partes[2]).size)
        assertEquals(32, Base64.getDecoder().decode(partes[3]).size)
    }

    @Test
    fun `la misma clave da hashes distintos por el salt`() {
        assertNotEquals(PasswordHasher.hash("Clave123"), PasswordHasher.hash("Clave123"))
    }

    @Test
    fun `un valor guardado que no tiene forma de hash no verifica`() {
        assertFalse(PasswordHasher.verificar("Clave123", "Clave123"))
        assertFalse(PasswordHasher.verificar("Clave123", "pbkdf2\$210000\$zz\$zz"))
        assertFalse(PasswordHasher.verificar("Clave123", ""))
    }

    // De aquí salen los hashes que están fijos en DatosSemilla:
    // gradlew testDebugUnitTest --tests "*PasswordHasherTest*" -Dsemilla=true
    @Test
    fun `genera los hashes de los usuarios semilla`() {
        assumeTrue(System.getProperty("semilla") == "true")

        for (id in listOf("u10001", "u10002", "u10003")) {
            println("$id ${PasswordHasher.hash("Clave123")}")
        }
    }
}
