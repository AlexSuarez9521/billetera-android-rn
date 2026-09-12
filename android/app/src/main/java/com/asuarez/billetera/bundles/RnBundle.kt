package com.asuarez.billetera.bundles

enum class RnBundle(val nombre: String, val componente: String, val privado: Boolean) {
    LOGIN("login", "LoginBundle", false),
    HOME("home", "HomeBundle", true),
    TRANSFERENCIA("transferencia", "TransferenciaBundle", true),
    MOVIMIENTOS("movimientos", "MovimientosBundle", true)
}
