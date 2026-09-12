package com.asuarez.billetera.bridge

import android.util.Log
import com.asuarez.billetera.BilleteraApp
import com.asuarez.billetera.bundles.BundleActivity
import com.asuarez.billetera.data.Estado
import com.asuarez.billetera.data.nombreCorto
import com.asuarez.billetera.security.BridgeCrypto
import com.asuarez.billetera.security.PayloadInvalido
import com.asuarez.billetera.session.Sesion
import com.asuarez.billetera.util.Resultado
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.UiThreadUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom
import java.util.Base64

private const val TAG = "Billetera"

/**
 * Única puerta de los bundles hacia Android. Entradas y salidas viajan cifradas con la llave de
 * canal; en claro solo quedan los nombres de evento, los códigos de error y [randomBytes].
 */
class BilleteraBridgeModule(private val ctx: ReactApplicationContext) :
    ReactContextBaseJavaModule(ctx) {

    private val aleatorio = SecureRandom()

    private val app: BilleteraApp
        get() = ctx.applicationContext as BilleteraApp

    override fun getName(): String = "BilleteraBridge"

    @ReactMethod
    fun randomBytes(n: Int, promise: Promise) {
        if (n <= 0 || n > 512) {
            promise.reject(BridgeErrores.PAYLOAD_INVALIDO, "Cantidad de bytes fuera de rango")
            return
        }
        val bytes = ByteArray(n)
        aleatorio.nextBytes(bytes)
        promise.resolve(Base64.getEncoder().encodeToString(bytes))
    }

    @ReactMethod
    fun login(envelope: String, promise: Promise) {
        runCatching {
            val datos = objeto(envelope)
            val r = app.banco.autenticar(datos.optString("celular"), datos.optString("clave"))
            when (r) {
                is Resultado.Error -> promise.reject(r.codigo, r.mensaje)
                is Resultado.Ok -> {
                    val s = app.sesion.crear(r.valor)
                    responder(
                        promise,
                        JSONObject()
                            .put("sessionId", s.sessionId)
                            .put("nombre", s.name)
                            .put("celular", s.phone)
                            .put("expiraEn", s.expiresAt)
                    )
                }
            }
        }.onFailure { fallo(promise, "login", it) }
    }

    @ReactMethod
    fun registrar(envelope: String, promise: Promise) {
        runCatching {
            val datos = objeto(envelope)
            val r = app.banco.registrar(
                datos.optString("nombre"),
                datos.optString("celular"),
                datos.optString("clave")
            )
            when (r) {
                is Resultado.Error -> promise.reject(r.codigo, r.mensaje)
                is Resultado.Ok -> {
                    val s = app.sesion.crear(r.valor)
                    responder(
                        promise,
                        JSONObject()
                            .put("sessionId", s.sessionId)
                            .put("nombre", s.name)
                            .put("celular", s.phone)
                            .put("expiraEn", s.expiresAt)
                    )
                }
            }
        }.onFailure { fallo(promise, "registrar", it) }
    }

    @ReactMethod
    fun buscarDestino(envelope: String, promise: Promise) {
        runCatching {
            val s = exigirSesion(promise) ?: return
            val celular = objeto(envelope).optString("celular")
            val destino = app.banco.buscarPorCelular(celular)
            when {
                destino == null ->
                    promise.reject(BridgeErrores.DESTINO_NO_EXISTE, "Ese celular no tiene cuenta")
                destino.id == s.userId ->
                    promise.reject(BridgeErrores.MISMA_CUENTA, "No puedes transferirte a ti mismo")
                destino.estado != Estado.ACTIVO ->
                    promise.reject(BridgeErrores.DESTINO_INACTIVO, "La cuenta destino está inactiva")
                else ->
                    responder(promise, JSONObject().put("nombre", destino.nombreCorto()))
            }
        }.onFailure { fallo(promise, "buscarDestino", it) }
    }

    @ReactMethod
    fun transferir(envelope: String, promise: Promise) {
        runCatching {
            val s = exigirSesion(promise) ?: return
            val datos = objeto(envelope)
            val descripcion = datos.optString("descripcion").takeIf { it.isNotBlank() }
            val r = app.banco.transferir(
                s.userId,
                datos.optString("celular"),
                datos.optLong("monto"),
                descripcion
            )
            when (r) {
                is Resultado.Error -> promise.reject(r.codigo, r.mensaje)
                is Resultado.Ok -> responder(
                    promise,
                    JSONObject()
                        .put("movimientoId", r.valor.movimientoId)
                        .put("nuevoSaldo", r.valor.nuevoSaldo)
                        .put("destinoNombre", r.valor.destinoNombre)
                        .put("fecha", r.valor.fecha)
                )
            }
        }.onFailure { fallo(promise, "transferir", it) }
    }

    @ReactMethod
    fun movimientos(promise: Promise) {
        runCatching {
            val s = exigirSesion(promise) ?: return
            val lista = JSONArray()
            for (m in app.banco.movimientos(s.userId)) {
                lista.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("fecha", m.fecha)
                        .put("tipo", m.tipo.name)
                        .put("valor", m.valor)
                        .put("descripcion", m.descripcion)
                        .put("estado", m.estado.name)
                )
            }
            responder(promise, JSONObject().put("movimientos", lista))
        }.onFailure { fallo(promise, "movimientos", it) }
    }

    @ReactMethod
    fun send(evento: String, envelope: String?, promise: Promise) {
        runCatching {
            val payload = envelope?.let { BridgeCrypto.descifrar(it) }
            UiThreadUtil.runOnUiThread(Runnable {
                val activity = ctx.currentActivity as? BundleActivity
                if (activity == null) {
                    Log.w(TAG, "Llegó $evento sin una pantalla de bundle en primer plano")
                } else {
                    // Acá ya salimos del runCatching de abajo: lo que se escape del manejador
                    // sube al hilo principal y tumba el proceso.
                    runCatching { activity.onBundleEvent(evento, payload) }
                        .onFailure { Log.e(TAG, "Falló atendiendo $evento: ${it.javaClass.simpleName}") }
                }
            })
            promise.resolve(null)
        }.onFailure { fallo(promise, "send $evento", it) }
    }

    private fun responder(promise: Promise, datos: JSONObject) {
        promise.resolve(BridgeCrypto.cifrar(datos.toString()))
    }

    // El mensaje de JSONException trae la entrada completa, o sea el payload en claro; por eso el
    // texto descifrado no se le pasa nunca directo al constructor de JSONObject sin este filtro.
    private fun objeto(envelope: String): JSONObject = try {
        JSONObject(BridgeCrypto.descifrar(envelope))
    } catch (e: JSONException) {
        throw PayloadInvalido("El payload descifrado no es un objeto JSON")
    }

    private fun exigirSesion(promise: Promise): Sesion? {
        val s = app.sesion.actual()
        if (s != null) return s

        promise.reject(BridgeErrores.SESION_INVALIDA, "Tu sesión expiró")
        // El aviso y la vuelta a Login los maneja la Activity, que es la que sabe si ya las
        // programó; si se hicieran también acá, un segundo toque dispararía dos navegaciones.
        UiThreadUtil.runOnUiThread(Runnable {
            (ctx.currentActivity as? BundleActivity)?.sesionExpiro()
        })
        return null
    }

    private fun fallo(promise: Promise, operacion: String, e: Throwable) {
        if (e is PayloadInvalido) {
            promise.reject(BridgeErrores.PAYLOAD_INVALIDO, "No pudimos procesar la solicitud")
            return
        }
        Log.e(TAG, "Falló $operacion: ${e.javaClass.simpleName}")
        promise.reject(BridgeErrores.ERROR_INTERNO, "Ocurrió un error, intenta de nuevo")
    }
}
