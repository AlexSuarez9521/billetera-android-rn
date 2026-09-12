package com.asuarez.billetera.data

// Los tres hashes salen de PasswordHasher con la misma clave de prueba; la clave está en el README.
private const val HASH_ALEX = "pbkdf2\$210000\$qXGOy5oObKKcI3LRFDFkqg==\$e8u2cqMbcm4kNDmO8gLKAh7iRBBRgZXOBSdYhbr/8CQ="
private const val HASH_LAURA = "pbkdf2\$210000\$eoYZgjEZvSy2Kwi2I5/fDA==\$kaX0TSiWqhUizusA7YWTrCekMiZsT8rCJNypNNIJF/w="
private const val HASH_CARLOS = "pbkdf2\$210000\$gJaJK+SolO5RQxQkarg3ZA==\$ASAjg5Xax1Zhfw5V3+RuR/PLOzEqE16ChkxROa8rkFk="

object DatosSemilla {

    val usuarios = listOf(
        Usuario("u10001", "Alex Suárez", "3001234567", HASH_ALEX, Estado.ACTIVO, 1_250_000),
        Usuario("u10002", "Laura Pérez", "3109876543", HASH_LAURA, Estado.ACTIVO, 480_000),
        Usuario("u10003", "Carlos Gómez", "3157654321", HASH_CARLOS, Estado.INACTIVO, 90_000)
    )

    val movimientos = listOf(
        Movimiento("m00001", "u10001", "2026-09-05T08:12:00", Tipo.CREDITO, 1_500_000, "Abono de nómina", EstadoMov.EXITOSA, null),
        Movimiento("m00002", "u10001", "2026-09-08T17:40:00", Tipo.DEBITO, 250_000, "Pago servicios públicos", EstadoMov.EXITOSA, null),
        Movimiento("m00003", "u10002", "2026-09-04T09:00:00", Tipo.CREDITO, 480_000, "Abono inicial", EstadoMov.EXITOSA, null)
    )
}
