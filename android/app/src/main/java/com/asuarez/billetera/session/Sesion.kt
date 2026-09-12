package com.asuarez.billetera.session

import org.json.JSONObject

data class Sesion(
    val sessionId: String,
    val userId: String,
    val name: String,
    val phone: String,
    val expiresAt: String,
    val expiraEnMs: Long
) {

    fun toJson(): String = JSONObject()
        .put("sessionId", sessionId)
        .put("userId", userId)
        .put("name", name)
        .put("phone", phone)
        .put("expiresAt", expiresAt)
        .put("expiraEnMs", expiraEnMs)
        .toString()

    companion object {
        // expiresAt queda en hora local porque es lo que se muestra; el vencimiento
        // se compara siempre contra expiraEnMs para no depender de la zona horaria.
        fun desdeJson(json: String): Sesion {
            val o = JSONObject(json)
            return Sesion(
                sessionId = o.getString("sessionId"),
                userId = o.getString("userId"),
                name = o.getString("name"),
                phone = o.getString("phone"),
                expiresAt = o.getString("expiresAt"),
                expiraEnMs = o.getLong("expiraEnMs")
            )
        }
    }
}
