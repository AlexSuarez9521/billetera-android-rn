package com.asuarez.billetera.security

import android.content.SharedPreferences
import android.util.Log
import java.security.GeneralSecurityException
import java.security.ProviderException
import java.util.Base64
import javax.crypto.SecretKey

interface KeyValueStore {
    fun get(clave: String): String?
    fun put(clave: String, valor: String)
    fun remove(clave: String)
    fun clear()
}

class SecureStore(
    private val prefs: SharedPreferences,
    private val llave: () -> SecretKey
) : KeyValueStore {

    override fun get(clave: String): String? {
        val guardado = prefs.getString(clave, null) ?: return null
        return try {
            val partes = guardado.split(".")
            require(partes.size == 2)
            val iv = Base64.getDecoder().decode(partes[0])
            val ct = Base64.getDecoder().decode(partes[1])
            String(AesGcm.descifrar(llave(), iv, ct), Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            descartar(clave, e)
            null
        } catch (e: IllegalArgumentException) {
            descartar(clave, e)
            null
        } catch (e: ProviderException) {
            // El Keystore avisa de sus propias averías con esta, que es RuntimeException y se
            // escapaba: sin capturarla, una llave inservible revienta la app en el arranque.
            descartar(clave, e)
            null
        }
    }

    override fun put(clave: String, valor: String) {
        try {
            val (iv, ct) = AesGcm.cifrar(llave(), valor.toByteArray(Charsets.UTF_8))
            val b64 = Base64.getEncoder()
            prefs.edit()
                .putString(clave, b64.encodeToString(iv) + "." + b64.encodeToString(ct))
                .apply()
        } catch (e: GeneralSecurityException) {
            noSeGuardo(clave, e)
        } catch (e: ProviderException) {
            noSeGuardo(clave, e)
        }
    }

    override fun remove(clave: String) {
        prefs.edit().remove(clave).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private fun descartar(clave: String, causa: Exception) {
        // Si el valor no abre (llave nueva, respaldo restaurado, manipulación) se bota;
        // lo que no se hace nunca es escribirlo en el log.
        Log.w("Billetera", "Valor ilegible en $clave: ${causa.javaClass.simpleName}")
        remove(clave)
    }

    // Lo peor que pasa si el cifrado falla es que la sesión no quede guardada y toque entrar otra
    // vez; lo que no puede pasar es que se caiga la pantalla que estaba escribiendo.
    private fun noSeGuardo(clave: String, causa: Exception) {
        Log.w("Billetera", "No se pudo guardar $clave: ${causa.javaClass.simpleName}")
        remove(clave)
    }
}
