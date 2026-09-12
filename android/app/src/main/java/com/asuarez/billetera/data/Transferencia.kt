package com.asuarez.billetera.data

data class Transferencia(
    val movimientoId: String,
    val nuevoSaldo: Long,
    val destinoNombre: String,
    val fecha: String
)
