package com.asuarez.billetera.nav

import android.content.Context
import android.content.Intent
import com.asuarez.billetera.bundles.HomeActivity
import com.asuarez.billetera.bundles.LoginActivity
import com.asuarez.billetera.bundles.MovimientosActivity
import com.asuarez.billetera.bundles.TransferenciaActivity
import com.asuarez.billetera.error.ErrorActivity

object Navegacion {

    const val EXTRA_MOTIVO = "motivo"

    fun aLogin(ctx: Context, motivo: String? = null) {
        val intent = Intent(ctx, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (motivo != null) intent.putExtra(EXTRA_MOTIVO, motivo)
        ctx.startActivity(intent)
    }

    fun aHome(ctx: Context) {
        ctx.startActivity(
            Intent(ctx, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    fun aTransferencia(ctx: Context) {
        ctx.startActivity(Intent(ctx, TransferenciaActivity::class.java))
    }

    fun aMovimientos(ctx: Context) {
        ctx.startActivity(Intent(ctx, MovimientosActivity::class.java))
    }

    fun aError(ctx: Context) {
        ctx.startActivity(Intent(ctx, ErrorActivity::class.java))
    }
}
