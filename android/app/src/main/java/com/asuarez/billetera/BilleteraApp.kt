package com.asuarez.billetera

import android.app.Application
import android.content.Context
import com.asuarez.billetera.data.BancoMockApi
import com.asuarez.billetera.data.MockDb
import com.asuarez.billetera.security.BridgeCrypto
import com.asuarez.billetera.security.LlaveKeystore
import com.asuarez.billetera.security.SecureStore
import com.asuarez.billetera.session.SesionManager
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative

class BilleteraApp : Application() {

    lateinit var sesion: SesionManager
        private set

    lateinit var banco: BancoMockApi
        private set

    override fun onCreate() {
        super.onCreate()
        // Desde 0.76 las .so vienen fusionadas en libreactnative.so y SoLoader necesita el mapping
        // que arma el plugin; por eso esto y no SoLoader.init(this, false).
        loadReactNative(this)
        BridgeCrypto.inicializar()

        val store = SecureStore(getSharedPreferences("billetera", MODE_PRIVATE), LlaveKeystore::obtener)
        sesion = SesionManager(store, ttlMinutos = BuildConfig.SESION_MINUTOS)
        banco = BancoMockApi(MockDb(store))
    }

    companion object {
        fun de(contexto: Context): BilleteraApp = contexto.applicationContext as BilleteraApp
    }
}
