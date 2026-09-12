package com.asuarez.billetera.data

import com.asuarez.billetera.security.KeyValueStore
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private const val CLAVE = "mockdb"

data class Apunte(val debito: Movimiento, val saldoOrigen: Long)

class MockDb(private val store: KeyValueStore) {

    private val usuarios = LinkedHashMap<String, Usuario>()
    private val movimientos = ArrayList<Movimiento>()
    private var cargada = false

    @Synchronized
    fun usuario(id: String): Usuario? {
        cargar()
        return usuarios[id]
    }

    @Synchronized
    fun porCelular(celular: String): Usuario? {
        cargar()
        return usuarios.values.firstOrNull { it.celular == celular }
    }

    @Synchronized
    fun movimientosDe(userId: String): List<Movimiento> {
        cargar()
        return movimientos.filter { it.userId == userId }
    }

    @Synchronized
    fun crearUsuario(nombre: String, celular: String, claveHash: String, saldo: Long): Usuario {
        cargar()
        val ultimo = usuarios.keys.mapNotNull { it.drop(1).toIntOrNull() }.maxOrNull() ?: 10000
        val nuevo = Usuario("u${ultimo + 1}", nombre, celular, claveHash, Estado.ACTIVO, saldo)
        usuarios[nuevo.id] = nuevo
        guardar()
        return nuevo
    }

    @Synchronized
    fun anotar(
        userId: String,
        fecha: String,
        tipo: Tipo,
        valor: Long,
        descripcion: String,
        estado: EstadoMov,
        contraparteCelular: String? = null
    ): Movimiento {
        cargar()
        val mov = Movimiento(nuevoIdMovimiento(), userId, fecha, tipo, valor, descripcion, estado, contraparteCelular)
        movimientos += mov
        guardar()
        return mov
    }

    // El saldo se vuelve a leer acá dentro y no afuera: la comprobación y el débito tienen que
    // caer bajo el mismo candado o dos transferencias a la vez dejan la cuenta en rojo. Los dos
    // apuntes van con un solo guardado, así que tampoco queda plata abonada sin su débito.
    @Synchronized
    fun aplicarTransferencia(
        origenId: String,
        destinoId: String,
        monto: Long,
        fecha: String,
        descripcionOrigen: String,
        descripcionDestino: String,
        descripcionRechazo: String
    ): Apunte? {
        cargar()
        val origen = usuarios.getValue(origenId)
        val destino = usuarios.getValue(destinoId)
        if (origen.saldo < monto) {
            movimientos += Movimiento(
                nuevoIdMovimiento(), origenId, fecha, Tipo.DEBITO, monto,
                descripcionRechazo, EstadoMov.RECHAZADA, destino.celular
            )
            guardar()
            return null
        }

        val saldoOrigen = origen.saldo - monto
        usuarios[origenId] = origen.copy(saldo = saldoOrigen)
        usuarios[destinoId] = destino.copy(saldo = destino.saldo + monto)

        val debito = Movimiento(
            nuevoIdMovimiento(), origenId, fecha, Tipo.DEBITO, monto,
            descripcionOrigen, EstadoMov.EXITOSA, destino.celular
        )
        movimientos += debito
        movimientos += Movimiento(
            nuevoIdMovimiento(), destinoId, fecha, Tipo.CREDITO, monto,
            descripcionDestino, EstadoMov.EXITOSA, origen.celular
        )
        guardar()
        return Apunte(debito, saldoOrigen)
    }

    private fun nuevoIdMovimiento(): String {
        val ultimo = movimientos.mapNotNull { it.id.drop(1).toIntOrNull() }.maxOrNull() ?: 0
        // Locale fijo: con el del sistema, en árabe %05d sale con otros dígitos y dos
        // movimientos podrían terminar con el mismo id.
        return String.format(Locale.ROOT, "m%05d", ultimo + 1)
    }

    private fun cargar() {
        if (cargada) return
        cargada = true
        val crudo = store.get(CLAVE)
        if (crudo != null && runCatching { leer(JSONObject(crudo)) }.isSuccess) return
        // Un JSON a medio leer deja las colecciones sucias, así que se vacían antes de sembrar.
        usuarios.clear()
        movimientos.clear()
        DatosSemilla.usuarios.forEach { usuarios[it.id] = it }
        movimientos += DatosSemilla.movimientos
        guardar()
    }

    private fun leer(raiz: JSONObject) {
        val us = raiz.getJSONArray("usuarios")
        for (i in 0 until us.length()) {
            val o = us.getJSONObject(i)
            val u = Usuario(
                o.getString("id"),
                o.getString("nombre"),
                o.getString("celular"),
                o.getString("claveHash"),
                Estado.valueOf(o.getString("estado")),
                o.getLong("saldo")
            )
            usuarios[u.id] = u
        }
        val ms = raiz.getJSONArray("movimientos")
        for (i in 0 until ms.length()) {
            val o = ms.getJSONObject(i)
            movimientos += Movimiento(
                o.getString("id"),
                o.getString("userId"),
                o.getString("fecha"),
                Tipo.valueOf(o.getString("tipo")),
                o.getLong("valor"),
                o.getString("descripcion"),
                EstadoMov.valueOf(o.getString("estado")),
                o.optString("contraparteCelular").ifEmpty { null }
            )
        }
    }

    private fun guardar() {
        val us = JSONArray()
        usuarios.values.forEach {
            us.put(
                JSONObject()
                    .put("id", it.id)
                    .put("nombre", it.nombre)
                    .put("celular", it.celular)
                    .put("claveHash", it.claveHash)
                    .put("estado", it.estado.name)
                    .put("saldo", it.saldo)
            )
        }
        val ms = JSONArray()
        for (m in movimientos) {
            val o = JSONObject()
                .put("id", m.id)
                .put("userId", m.userId)
                .put("fecha", m.fecha)
                .put("tipo", m.tipo.name)
                .put("valor", m.valor)
                .put("descripcion", m.descripcion)
                .put("estado", m.estado.name)
            if (m.contraparteCelular != null) o.put("contraparteCelular", m.contraparteCelular)
            ms.put(o)
        }
        store.put(CLAVE, JSONObject().put("usuarios", us).put("movimientos", ms).toString())
    }
}
