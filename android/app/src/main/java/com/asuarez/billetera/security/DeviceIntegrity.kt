package com.asuarez.billetera.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.util.concurrent.TimeUnit

data class Diagnostico(val rooteado: Boolean, val emulador: Boolean, val senales: List<String>)

object DeviceIntegrity {

    // Las pruebas instrumentadas lo dejan en true para que el aviso no tape la pantalla.
    @Volatile
    var omitirAvisoEnPruebas = false

    private val rutasSu = listOf(
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/su/bin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    private val appsDeRoot = listOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser"
    )

    // TODO: evaluar Play Integrity en vez de heurísticas locales
    fun diagnosticar(ctx: Context): Diagnostico {
        val root = senalesDeRoot(ctx)
        val emulador = senalesDeEmulador()
        return Diagnostico(root.isNotEmpty(), emulador.isNotEmpty(), root + emulador)
    }

    private fun senalesDeRoot(ctx: Context): List<String> {
        val senales = mutableListOf<String>()
        for (ruta in rutasSu) {
            if (File(ruta).exists()) senales += "su en $ruta"
        }
        if (Build.TAGS?.contains("test-keys") == true) senales += "TAGS=${Build.TAGS}"
        for (paquete in appsDeRoot) {
            try {
                ctx.packageManager.getPackageInfo(paquete, 0)
                senales += "app $paquete"
            } catch (e: PackageManager.NameNotFoundException) {
                // no está instalada, que es lo normal
            }
        }
        if (respondeWhichSu()) senales += "which su responde"
        return senales
    }

    private fun senalesDeEmulador(): List<String> {
        val senales = mutableListOf<String>()
        val hardware = Build.HARDWARE.lowercase()
        if (hardware == "goldfish" || hardware == "ranchu") senales += "HARDWARE=$hardware"

        val producto = Build.PRODUCT.lowercase()
        if (producto.startsWith("sdk") || producto.contains("sdk_gphone") || producto.contains("emulator")) {
            senales += "PRODUCT=$producto"
        }

        val dispositivo = Build.DEVICE.lowercase()
        if (dispositivo.startsWith("emu64") || dispositivo.startsWith("generic")) {
            senales += "DEVICE=$dispositivo"
        }

        val huella = Build.FINGERPRINT.lowercase()
        if (huella.contains("emu64") || huella.contains("emulator") || huella.contains("generic")) {
            senales += "FINGERPRINT de emulador"
        }

        if (Build.BOARD.lowercase().contains("goldfish")) senales += "BOARD=${Build.BOARD}"
        if (Build.BOOTLOADER == "unknown") senales += "BOOTLOADER=unknown"
        if (propSistema("ro.kernel.qemu") == "1" || propSistema("ro.boot.qemu") == "1") senales += "qemu=1"
        return senales
    }

    // La salida se lee después del waitFor y no antes: readText() se queda esperando el EOF, o sea
    // el final del proceso, y el timeout no llegaría a aplicarse nunca. Son dos comandos con una
    // línea de salida, así que no hay riesgo de llenar el buffer de la tubería.
    private fun respondeWhichSu(): Boolean = try {
        val proceso = ProcessBuilder("which", "su").redirectErrorStream(true).start()
        if (!proceso.waitFor(1, TimeUnit.SECONDS)) {
            proceso.destroyForcibly()
            false
        } else {
            proceso.inputStream.bufferedReader().use { it.readText() }.trim().isNotEmpty()
        }
    } catch (e: Exception) {
        false
    }

    private fun propSistema(nombre: String): String? = try {
        val proceso = ProcessBuilder("/system/bin/getprop", nombre).redirectErrorStream(true).start()
        if (!proceso.waitFor(1, TimeUnit.SECONDS)) {
            proceso.destroyForcibly()
            null
        } else {
            proceso.inputStream.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
        }
    } catch (e: Exception) {
        null
    }
}
