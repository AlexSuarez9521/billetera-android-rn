package com.asuarez.billetera.bridge

object BridgeEvents {
    const val LOGIN_SUCCESS = "LOGIN_SUCCESS"
    const val HOME_READY = "HOME_READY"
    const val LOAD_HOME = "LOAD_HOME"
    const val OPEN_TRANSFER = "OPEN_TRANSFER"
    const val OPEN_MOVEMENTS = "OPEN_MOVEMENTS"
    const val TRANSFER_SUCCESS = "TRANSFER_SUCCESS"
    const val LOGOUT = "LOGOUT"
    const val SESSION_EXPIRED = "SESSION_EXPIRED"
}

// Los códigos que salen de las reglas de negocio los pone BancoMockApi; aquí solo están los que
// nacen en el puente.
object BridgeErrores {
    const val SESION_INVALIDA = "SESION_INVALIDA"
    const val PAYLOAD_INVALIDO = "PAYLOAD_INVALIDO"
    const val ERROR_INTERNO = "ERROR_INTERNO"
    const val DESTINO_NO_EXISTE = "DESTINO_NO_EXISTE"
    const val DESTINO_INACTIVO = "DESTINO_INACTIVO"
    const val MISMA_CUENTA = "MISMA_CUENTA"
}
