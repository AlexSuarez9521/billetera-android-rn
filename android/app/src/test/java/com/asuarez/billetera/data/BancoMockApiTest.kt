package com.asuarez.billetera.data

import com.asuarez.billetera.util.Resultado
import com.asuarez.billetera.util.StoreEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val CLAVE_SEMILLA = "Clave123"

// 2026-09-11, posterior a las fechas de la semilla, para que el orden de los movimientos no dependa del reloj real.
private const val AHORA = 1_789_084_800_000L

class BancoMockApiTest {

    private lateinit var store: StoreEnMemoria
    private lateinit var banco: BancoMockApi

    @Before
    fun preparar() {
        store = StoreEnMemoria()
        banco = BancoMockApi(MockDb(store), reloj = { AHORA }, conLatencia = false)
    }

    @Test
    fun `login con la clave correcta devuelve el usuario`() {
        val u = exito(banco.autenticar("3001234567", CLAVE_SEMILLA))
        assertEquals("u10001", u.id)
        assertEquals("Alex Suárez", u.nombre)
        assertEquals(1_250_000L, u.saldo)
    }

    @Test
    fun `celular desconocido y clave equivocada dan el mismo error`() {
        assertEquals("CREDENCIALES_INVALIDAS", codigo(banco.autenticar("3001234567", "otraClave")))
        assertEquals("CREDENCIALES_INVALIDAS", codigo(banco.autenticar("3009999999", CLAVE_SEMILLA)))
        assertEquals("CAMPOS_OBLIGATORIOS", codigo(banco.autenticar("", "")))
        assertEquals("CELULAR_INVALIDO", codigo(banco.autenticar("30012", CLAVE_SEMILLA)))
    }

    @Test
    fun `una cuenta inactiva no puede entrar`() {
        assertEquals("USUARIO_INACTIVO", codigo(banco.autenticar("3157654321", CLAVE_SEMILLA)))
    }

    @Test
    fun `el registro abre la cuenta con el abono de bienvenida`() {
        val nuevo = exito(banco.registrar("Sofía Ramírez", "3012223344", "Segura2026"))
        assertEquals("u10004", nuevo.id)
        assertEquals(Estado.ACTIVO, nuevo.estado)
        assertEquals(50_000L, nuevo.saldo)

        val movs = banco.movimientos(nuevo.id)
        assertEquals(1, movs.size)
        assertEquals(Tipo.CREDITO, movs[0].tipo)
        assertEquals("Abono de bienvenida", movs[0].descripcion)
        assertEquals(EstadoMov.EXITOSA, movs[0].estado)

        assertEquals(nuevo.id, exito(banco.autenticar("3012223344", "Segura2026")).id)
    }

    @Test
    fun `no se puede registrar un celular que ya tiene cuenta`() {
        assertEquals("USUARIO_YA_EXISTE", codigo(banco.registrar("Alex Suárez", "3001234567", "Segura2026")))
    }

    @Test
    fun `el registro exige clave de al menos seis caracteres y celular valido`() {
        assertEquals("CLAVE_DEBIL", codigo(banco.registrar("Sofía Ramírez", "3012223344", "12345")))
        assertEquals("CELULAR_INVALIDO", codigo(banco.registrar("Sofía Ramírez", "3012", "Segura2026")))
        assertEquals("CAMPOS_OBLIGATORIOS", codigo(banco.registrar("", "3012223344", "Segura2026")))
    }

    @Test
    fun `una transferencia exitosa deja debito en el origen y credito en el destino`() {
        val t = exito(banco.transferir("u10001", "3109876543", 120_000, "almuerzo"))
        assertEquals("Laura P.", t.destinoNombre)
        assertEquals(1_130_000L, t.nuevoSaldo)

        assertEquals(1_130_000L, banco.usuario("u10001")!!.saldo)
        assertEquals(600_000L, banco.usuario("u10002")!!.saldo)

        val enAlex = banco.movimientos("u10001").first()
        assertEquals(t.movimientoId, enAlex.id)
        assertEquals(Tipo.DEBITO, enAlex.tipo)
        assertEquals("Transferencia a Laura P. - almuerzo", enAlex.descripcion)
        assertEquals("3109876543", enAlex.contraparteCelular)

        val enLaura = banco.movimientos("u10002").first()
        assertEquals(Tipo.CREDITO, enLaura.tipo)
        assertEquals("Transferencia de Alex S.", enLaura.descripcion)
        assertEquals(enAlex.fecha, enLaura.fecha)
    }

    @Test
    fun `sin descripcion el movimiento se queda con el texto base`() {
        exito(banco.transferir("u10001", "3109876543", 10_000, null))
        assertEquals("Transferencia a Laura P.", banco.movimientos("u10001").first().descripcion)
    }

    @Test
    fun `sin saldo suficiente queda el movimiento rechazado y no se mueve la plata`() {
        val r = banco.transferir("u10002", "3001234567", 900_000, null)
        assertEquals("SALDO_INSUFICIENTE", codigo(r))
        assertEquals(480_000L, banco.usuario("u10002")!!.saldo)
        assertEquals(1_250_000L, banco.usuario("u10001")!!.saldo)

        val rechazado = banco.movimientos("u10002").first()
        assertEquals(EstadoMov.RECHAZADA, rechazado.estado)
        assertEquals(Tipo.DEBITO, rechazado.tipo)
        assertEquals("Transferencia rechazada: saldo insuficiente", rechazado.descripcion)
        assertEquals(2, banco.movimientos("u10001").size)
    }

    @Test
    fun `no se puede transferir a la propia cuenta`() {
        assertEquals("MISMA_CUENTA", codigo(banco.transferir("u10001", "3001234567", 5_000, null)))
    }

    @Test
    fun `destino que no existe`() {
        assertEquals("DESTINO_NO_EXISTE", codigo(banco.transferir("u10001", "3009999999", 5_000, null)))
        assertEquals(2, banco.movimientos("u10001").size)
    }

    @Test
    fun `no se puede transferir a una cuenta inactiva`() {
        assertEquals("DESTINO_INACTIVO", codigo(banco.transferir("u10001", "3157654321", 5_000, null)))
    }

    @Test
    fun `monto en cero o negativo se rechaza`() {
        assertEquals("MONTO_INVALIDO", codigo(banco.transferir("u10001", "3109876543", 0, null)))
        assertEquals("MONTO_INVALIDO", codigo(banco.transferir("u10001", "3109876543", -1_000, null)))
        assertEquals(1_250_000L, banco.usuario("u10001")!!.saldo)
    }

    @Test
    fun `movimientos trae solo los del usuario y el mas reciente primero`() {
        val alex = banco.movimientos("u10001")
        assertEquals(2, alex.size)
        assertTrue(alex.all { it.userId == "u10001" })
        assertEquals("Pago servicios públicos", alex[0].descripcion)
        assertEquals("Abono de nómina", alex[1].descripcion)
        assertEquals(1, banco.movimientos("u10002").size)
        assertTrue(banco.movimientos("u99999").isEmpty())
    }

    @Test
    fun `ni la respuesta ni lo persistido llevan la clave en texto plano`() {
        val u = exito(banco.autenticar("3001234567", CLAVE_SEMILLA))
        assertFalse(u.toString().contains(CLAVE_SEMILLA))
        assertTrue(u.claveHash.startsWith("pbkdf2\$210000\$"))
        assertFalse(store.contiene(CLAVE_SEMILLA))
    }

    @Test
    fun `el estado sobrevive a una instancia nueva sobre el mismo store`() {
        exito(banco.transferir("u10001", "3109876543", 50_000, null))

        val otro = BancoMockApi(MockDb(store), conLatencia = false)
        assertEquals(1_200_000L, otro.usuario("u10001")!!.saldo)
        assertEquals(530_000L, otro.usuario("u10002")!!.saldo)
        assertEquals(3, otro.movimientos("u10001").size)
        assertNotNull(otro.buscarPorCelular("3109876543"))
    }

    @Test
    fun `un valor corrupto en el store vuelve a la semilla`() {
        store.put("mockdb", "{esto no es json")
        val otro = BancoMockApi(MockDb(store), conLatencia = false)
        assertEquals(1_250_000L, otro.usuario("u10001")!!.saldo)
    }
}

private fun <T> exito(r: Resultado<T>): T = when (r) {
    is Resultado.Ok -> r.valor
    is Resultado.Error -> throw AssertionError("se esperaba Ok y llegó ${r.codigo}")
}

private fun codigo(r: Resultado<*>): String = when (r) {
    is Resultado.Error -> r.codigo
    is Resultado.Ok -> throw AssertionError("se esperaba un error y llegó ${r.valor}")
}
