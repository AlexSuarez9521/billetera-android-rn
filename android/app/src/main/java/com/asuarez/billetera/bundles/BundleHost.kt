@file:Suppress("DEPRECATION")

package com.asuarez.billetera.bundles

import android.app.Application
import android.content.Context
import com.asuarez.billetera.bridge.BilleteraBridgePackage
import com.facebook.react.PackageList
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JSExceptionHandler
import com.facebook.react.common.SurfaceDelegateFactory
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.devsupport.DevSupportManagerFactory
import com.facebook.react.devsupport.ReactInstanceDevHelper
import com.facebook.react.devsupport.ReleaseDevSupportManager
import com.facebook.react.devsupport.interfaces.DevBundleDownloadListener
import com.facebook.react.devsupport.interfaces.DevLoadingViewManager
import com.facebook.react.devsupport.interfaces.DevSupportManager
import com.facebook.react.devsupport.interfaces.PausedInDebuggerOverlayManager
import com.facebook.react.devsupport.interfaces.RedBoxHandler
import com.facebook.react.packagerconnection.RequestHandler

class BundleHost(
    private val app: Application,
    val bundle: RnBundle,
    private val alFallar: (Throwable) -> Unit
) : DefaultReactNativeHost(app) {

    override fun getPackages(): List<ReactPackage> =
        PackageList(app).packages + BilleteraBridgePackage()

    override fun getJSMainModuleName(): String = "index"

    override fun getBundleAssetName(): String = "bundles/${bundle.nombre}.android.bundle"

    override fun getUseDeveloperSupport(): Boolean = false

    override val isNewArchEnabled: Boolean = false

    override fun getJSExceptionHandler(): JSExceptionHandler = JSExceptionHandler { alFallar(it) }

    // El JSExceptionHandler solo cubre lo que revienta dentro de JS. Si el bundle ni siquiera
    // carga, ReactInstanceManager le pasa la excepción al DevSupportManager, y el de release la
    // relanza y se lleva el proceso; con esta fábrica alcanzamos a mostrar la pantalla de error.
    override fun getDevSupportManagerFactory(): DevSupportManagerFactory = FabricaDeSoporte(alFallar)
}

private class FabricaDeSoporte(private val alFallar: (Throwable) -> Unit) : DevSupportManagerFactory {

    override fun create(
        applicationContext: Context,
        reactInstanceManagerHelper: ReactInstanceDevHelper,
        packagerPathForJSBundleName: String?,
        enableOnCreate: Boolean,
        redBoxHandler: RedBoxHandler?,
        devBundleDownloadListener: DevBundleDownloadListener?,
        minNumShakes: Int,
        customPackagerCommandHandlers: Map<String, RequestHandler>?,
        surfaceDelegateFactory: SurfaceDelegateFactory?,
        devLoadingViewManager: DevLoadingViewManager?,
        pausedInDebuggerOverlayManager: PausedInDebuggerOverlayManager?
    ): DevSupportManager = SoporteSinRedbox(alFallar)

    override fun create(
        applicationContext: Context,
        reactInstanceManagerHelper: ReactInstanceDevHelper,
        packagerPathForJSBundleName: String?,
        enableOnCreate: Boolean,
        redBoxHandler: RedBoxHandler?,
        devBundleDownloadListener: DevBundleDownloadListener?,
        minNumShakes: Int,
        customPackagerCommandHandlers: Map<String, RequestHandler>?,
        surfaceDelegateFactory: SurfaceDelegateFactory?,
        devLoadingViewManager: DevLoadingViewManager?,
        pausedInDebuggerOverlayManager: PausedInDebuggerOverlayManager?,
        useDevSupport: Boolean
    ): DevSupportManager = SoporteSinRedbox(alFallar)
}

private class SoporteSinRedbox(private val alFallar: (Throwable) -> Unit) : ReleaseDevSupportManager() {
    override fun handleException(e: Exception) {
        alFallar(e)
    }
}
