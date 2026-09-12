package com.asuarez.billetera.util

import com.asuarez.billetera.security.KeyValueStore

class StoreEnMemoria : KeyValueStore {

    private val datos = mutableMapOf<String, String>()

    override fun get(clave: String): String? = datos[clave]

    override fun put(clave: String, valor: String) {
        datos[clave] = valor
    }

    override fun remove(clave: String) {
        datos.remove(clave)
    }

    override fun clear() {
        datos.clear()
    }

    fun vacio(): Boolean = datos.isEmpty()

    fun contiene(texto: String): Boolean = datos.values.any { it.contains(texto) }
}
