package com.asuarez.billetera.data

import com.asuarez.billetera.security.PasswordHasher
import com.asuarez.billetera.util.Fechas
import com.asuarez.billetera.util.Resultado
import kotlin.random.Random

private const val SALDO_BIENVENIDA = 50_000L

// TODO: cuando exista backend, esto es un cliente HTTP y la latencia simulada sale
class BancoMockApi(
    private val db: MockDb,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val conLatencia: Boolean = true
) {

    fun autenticar(celular: String, clave: String): Resultado<Usuario> {
        if (celular.isBlank() || clave.isBlank()) {
            return Resultado.Error("CAMPOS_OBLIGATORIOS", "Completa todos los campos")
        }
        if (!celularValido(celular)) {
            return Resultado.Error("CELULAR_INVALIDO", "El celular debe tener 10 dígitos y empezar por 3")
        }
        demorar()
        val u = db.porCelular(celular)
        if (u == null || !PasswordHasher.verificar(clave, u.claveHash)) {
            return Resultado.Error("CREDENCIALES_INVALIDAS", "Celular o clave incorrectos")
        }
        // El estado se mira después de verificar la clave; si no, con solo el celular se sabría
        // qué cuentas existen y cuáles están bloqueadas.
        if (u.estado != Estado.ACTIVO) {
            return Resultado.Error("USUARIO_INACTIVO", "Tu cuenta está inactiva, comunícate con soporte")
        }
        return Resultado.Ok(u)
    }

    fun registrar(nombre: String, celular: String, clave: String): Resultado<Usuario> {
        if (nombre.isBlank() || celular.isBlank() || clave.isBlank()) {
            return Resultado.Error("CAMPOS_OBLIGATORIOS", "Completa todos los campos")
        }
        if (!celularValido(celular)) {
            return Resultado.Error("CELULAR_INVALIDO", "El celular debe tener 10 dígitos y empezar por 3")
        }
        if (clave.length < 6) {
            return Resultado.Error("CLAVE_DEBIL", "La clave debe tener al menos 6 caracteres")
        }
        if (db.porCelular(celular) != null) {
            return Resultado.Error("USUARIO_YA_EXISTE", "Ese celular ya tiene una cuenta")
        }
        val nuevo = db.crearUsuario(nombre.trim(), celular, PasswordHasher.hash(clave), SALDO_BIENVENIDA)
        db.anotar(
            nuevo.id, Fechas.isoDesdeMs(reloj()), Tipo.CREDITO, SALDO_BIENVENIDA,
            "Abono de bienvenida", EstadoMov.EXITOSA
        )
        return Resultado.Ok(nuevo)
    }

    fun usuario(id: String): Usuario? = db.usuario(id)

    fun buscarPorCelular(celular: String): Usuario? = db.porCelular(celular)

    fun transferir(
        origenId: String,
        celularDestino: String,
        monto: Long,
        descripcion: String?
    ): Resultado<Transferencia> {
        val origen = db.usuario(origenId)
            ?: return Resultado.Error("SESION_INVALIDA", "Tu sesión expiró")
        if (!celularValido(celularDestino)) {
            return Resultado.Error("CELULAR_INVALIDO", "El celular debe tener 10 dígitos y empezar por 3")
        }
        if (monto <= 0) {
            return Resultado.Error("MONTO_INVALIDO", "Ingresa un monto mayor a cero")
        }
        if (celularDestino == origen.celular) {
            return Resultado.Error("MISMA_CUENTA", "No puedes transferirte a ti mismo")
        }
        demorar()
        val destino = db.porCelular(celularDestino)
            ?: return Resultado.Error("DESTINO_NO_EXISTE", "Ese celular no tiene cuenta")
        if (destino.estado != Estado.ACTIVO) {
            return Resultado.Error("DESTINO_INACTIVO", "La cuenta destino está inactiva")
        }

        val fecha = Fechas.isoDesdeMs(reloj())
        val detalle = descripcion?.trim().orEmpty()
        val enOrigen = "Transferencia a ${destino.nombreCorto()}" + if (detalle.isEmpty()) "" else " - $detalle"
        // El saldo se revisa dentro del mismo método sincronizado que descuenta; acá arriba ya
        // pasó la demora y el dato estaría viejo. El intento fallido queda en el extracto, que es
        // lo que espera ver alguien que revisa su cuenta.
        val apunte = db.aplicarTransferencia(
            origen.id, destino.id, monto, fecha, enOrigen,
            "Transferencia de ${origen.nombreCorto()}",
            "Transferencia rechazada: saldo insuficiente"
        ) ?: return Resultado.Error("SALDO_INSUFICIENTE", "No tienes saldo suficiente")

        return Resultado.Ok(
            Transferencia(apunte.debito.id, apunte.saldoOrigen, destino.nombreCorto(), fecha)
        )
    }

    fun movimientos(userId: String): List<Movimiento> =
        db.movimientosDe(userId).sortedByDescending { it.fecha }

    // Corre en el hilo de módulos nativos, nunca en el de UI.
    private fun demorar() {
        if (!conLatencia) return
        Thread.sleep(200L + Random.nextInt(201))
    }

    private fun celularValido(celular: String) =
        celular.length == 10 && celular[0] == '3' && celular.all { it.isDigit() }
}
