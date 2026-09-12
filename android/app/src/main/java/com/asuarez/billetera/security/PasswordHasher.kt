package com.asuarez.billetera.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    // OWASP sugiere 600k en servidor; aquí el hash corre en el teléfono y con 210k
    // el login se queda por debajo de medio segundo en los equipos donde lo probé.
    private const val ITERACIONES = 210_000
    private const val BITS = 256
    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    private const val SEPARADOR = "\$"

    private val azar = SecureRandom()

    fun hash(clave: String): String {
        val salt = ByteArray(16).also { azar.nextBytes(it) }
        val b64 = Base64.getEncoder()
        return listOf(
            "pbkdf2",
            ITERACIONES.toString(),
            b64.encodeToString(salt),
            b64.encodeToString(derivar(clave, salt, ITERACIONES))
        ).joinToString(SEPARADOR)
    }

    fun verificar(clave: String, guardado: String): Boolean {
        val partes = guardado.split(SEPARADOR)
        if (partes.size != 4 || partes[0] != "pbkdf2") return false
        return try {
            val iteraciones = partes[1].toInt()
            val salt = Base64.getDecoder().decode(partes[2])
            val esperado = Base64.getDecoder().decode(partes[3])
            MessageDigest.isEqual(esperado, derivar(clave, salt, iteraciones))
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun derivar(clave: String, salt: ByteArray, iteraciones: Int): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), salt, iteraciones, BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
