package com.lindum.mlkit.vision

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.PixelCopy.OnPixelCopyFinishedListener
import android.view.SurfaceView

internal object PixelCopyHelper {
    @JvmStatic
    fun getSurfaceBitmap(surfaceView: SurfaceView, listener: PixelCopyListener) {
        val bitmap = Bitmap.createBitmap(
            surfaceView.getWidth(),
            surfaceView.getHeight(),
            Bitmap.Config.ARGB_8888
        )
        val handlerThread = HandlerThread(PixelCopyHelper::class.java.getSimpleName())
        handlerThread.start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(surfaceView, bitmap, OnPixelCopyFinishedListener { copyResult: Int ->
                if (copyResult == PixelCopy.SUCCESS) {
                    listener.onSurfaceBitmapReady(bitmap)
                } else {
                    listener.onSurfaceBitmapError("Couldn't create bitmap of the SurfaceView")
                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.getLooper()))
        } else {
            listener.onSurfaceBitmapError("Saving an image of a SurfaceView is only supported for API 24 and above")
        }
    }

    internal interface PixelCopyListener {
        fun onSurfaceBitmapReady(bitmap: Bitmap?)

        fun onSurfaceBitmapError(errorMsg: String?)
    }
}
