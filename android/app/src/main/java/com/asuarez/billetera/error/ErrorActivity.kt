package com.asuarez.billetera.error

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.asuarez.billetera.BilleteraApp
import com.asuarez.billetera.R
import com.asuarez.billetera.nav.Navegacion

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        findViewById<Button>(R.id.volver).setOnClickListener {
            if (BilleteraApp.de(this).sesion.esValida()) Navegacion.aHome(this)
            else Navegacion.aLogin(this)
            finish()
        }
    }
}
