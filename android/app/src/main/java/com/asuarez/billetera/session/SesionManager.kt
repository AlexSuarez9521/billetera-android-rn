package com.asuarez.billetera.session

import android.util.Log
import com.asuarez.billetera.data.Usuario
import com.asuarez.billetera.security.KeyValueStore
import com.asuarez.billetera.util.Fechas
import org.json.JSONException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Crea, lee y cierra la sesión guardada en el store cifrado. El reloj se inyecta
 * para poder adelantar el tiempo en las pruebas.
 */
class SesionManager(
    private val store: KeyValueStore,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val ttlMinutos: Int
) {

    fun crear(usuario: Usuario): Sesion {
        val expira = reloj() + TimeUnit.MINUTES.toMillis(ttlMinutos.toLong())
        val sesion = Sesion(
            sessionId = UUID.randomUUID().toString(),
            userId = usuario.id,
            name = usuario.nombre,
            phone = usuario.celular,
            expiresAt = Fechas.isoDesdeMs(expira),
            expiraEnMs = expira
        )
        store.put(CLAVE, sesion.toJson())
        return sesion
    }

    fun actual(): Sesion? {
        val guardado = store.get(CLAVE) ?: return null
        val sesion = try {
            Sesion.desdeJson(guardado)
        } catch (e: JSONException) {
            Log.w("Billetera", "Sesión ilegible, se cierra")
            store.remove(CLAVE)
            return null
        }
        // TODO: renovar la expiración con actividad; hoy es TTL fijo
        if (sesion.expiraEnMs <= reloj()) {
            store.remove(CLAVE)
            return null
        }
        return sesion
    }

    fun esValida(): Boolean = actual() != null

    fun cerrar() {
        store.remove(CLAVE)
    }

    private companion object {
        const val CLAVE = "sesion"
    }
}
