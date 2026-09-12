package com.asuarez.billetera.data

data class Movimiento(
    val id: String,
    val userId: String,
    val fecha: String,
    val tipo: Tipo,
    val valor: Long,
    val descripcion: String,
    val estado: EstadoMov,
    val contraparteCelular: String?
)

enum class Tipo { DEBITO, CREDITO }

enum class EstadoMov { EXITOSA, RECHAZADA }
