package com.asuarez.billetera.data

data class Usuario(
    val id: String,
    val nombre: String,
    val celular: String,
    val claveHash: String,
    val estado: Estado,
    val saldo: Long
)

enum class Estado { ACTIVO, INACTIVO }

// Quien va a transferir solo necesita reconocer la cuenta, no saber el apellido completo.
fun Usuario.nombreCorto(): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    if (partes.size < 2) return nombre.trim()
    return partes[0] + " " + partes[1].first().uppercaseChar() + "."
}
