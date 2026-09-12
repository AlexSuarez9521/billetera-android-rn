package com.asuarez.billetera.security

import org.json.JSONException
import org.json.JSONObject
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

/**
 * Cifra lo que viaja entre Android y los bundles. La llave del canal vive solo en
 * memoria, se le entrega a JS en las props iniciales y se rota al volver a Login.
 */
object BridgeCrypto {

    private val azar = SecureRandom()

    @Volatile
    private var clave: ByteArray? = null

    @Synchronized
    fun inicializar() {
        if (clave == null) clave = nuevaClave()
    }

    @Synchronized
    fun rotar() {
        clave = nuevaClave()
    }

    fun claveBase64(): String = Base64.getEncoder().encodeToString(bytesDeLlave())

    fun cifrar(json: String): String {
        val (iv, ct) = AesGcm.cifrar(llave(), json.toByteArray(Charsets.UTF_8))
        val b64 = Base64.getEncoder()
        return JSONObject()
            .put("iv", b64.encodeToString(iv))
            .put("data", b64.encodeToString(ct))
            .toString()
    }

    fun descifrar(envelope: String): String {
        return try {
            val sobre = JSONObject(envelope)
            val iv = Base64.getDecoder().decode(sobre.getString("iv"))
            val ct = Base64.getDecoder().decode(sobre.getString("data"))
            String(AesGcm.descifrar(llave(), iv, ct), Charsets.UTF_8)
        } catch (e: JSONException) {
            throw PayloadInvalido("El envelope no tiene iv y data")
        } catch (e: IllegalArgumentException) {
            throw PayloadInvalido("Base64 inválido en el envelope")
        } catch (e: GeneralSecurityException) {
            throw PayloadInvalido("El payload no descifra")
        }
    }

    private fun nuevaClave() = ByteArray(32).also { azar.nextBytes(it) }

    private fun bytesDeLlave() =
        clave ?: throw IllegalStateException("BridgeCrypto sin inicializar")

    private fun llave() = SecretKeySpec(bytesDeLlave(), "AES")
}

class PayloadInvalido(mensaje: String) : Exception(mensaje)
