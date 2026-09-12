package com.asuarez.billetera.util

sealed class Resultado<out T> {
    data class Ok<T>(val valor: T) : Resultado<T>()
    data class Error(val codigo: String, val mensaje: String) : Resultado<Nothing>()
}
