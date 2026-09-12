package com.asuarez.billetera.security

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AesGcm {

    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val BITS_TAG = 128

    fun cifrar(llave: SecretKey, plano: ByteArray): Pair<ByteArray, ByteArray> {
        // Sin GCMParameterSpec a propósito: con llaves del Keystore, Android genera
        // el IV él mismo y rechaza el que uno le pase.
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, llave)
        return cipher.iv to cipher.doFinal(plano)
    }

    fun descifrar(llave: SecretKey, iv: ByteArray, ct: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.DECRYPT_MODE, llave, GCMParameterSpec(BITS_TAG, iv))
        return cipher.doFinal(ct)
    }

    // Solo sirve para comprobar el vector compartido con JS. En la app el IV nunca lo elige el llamador.
    internal fun cifrarConIv(llave: SecretKey, iv: ByteArray, plano: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, llave, GCMParameterSpec(BITS_TAG, iv))
        return cipher.doFinal(plano)
    }
}
