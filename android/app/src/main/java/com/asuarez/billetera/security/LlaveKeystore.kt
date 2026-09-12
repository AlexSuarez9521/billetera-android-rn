package com.asuarez.billetera.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.ProviderException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object LlaveKeystore {

    // El v1 del alias deja cambiar el formato guardado más adelante sin pelear con los datos viejos.
    private const val ALIAS = "billetera.store.v1"
    private const val KEYSTORE = "AndroidKeyStore"

    @Synchronized
    fun obtener(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        val existente = try {
            ks.getKey(ALIAS, null) as? SecretKey
        } catch (e: KeyStoreException) {
            botarAlias(ks, e)
        } catch (e: ProviderException) {
            botarAlias(ks, e)
        }
        if (existente != null) return existente

        val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        keygen.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keygen.generateKey()
    }

    // Un alias averiado (keystore corrupto, llave que el sistema ya invalidó) se bota y se vuelve
    // a generar: lo que se pierde es la sesión guardada, no el acceso a la app.
    private fun botarAlias(ks: KeyStore, causa: Exception): SecretKey? {
        Log.w("Billetera", "Alias del keystore inservible: ${causa.javaClass.simpleName}")
        runCatching { ks.deleteEntry(ALIAS) }
        return null
    }
}
