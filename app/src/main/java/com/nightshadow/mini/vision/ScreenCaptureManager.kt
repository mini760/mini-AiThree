package com.nightshadow.mini.vision

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import com.nightshadow.mini.diagnostics.MiniLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@SuppressLint("StaticFieldLeak")
object ScreenCaptureManager {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var context: Context? = null

    fun init(ctx: Context, resultCode: Int, data: Intent) {
        context = ctx.applicationContext
        val mpm = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpm.getMediaProjection(resultCode, data)
        MiniLogger.i("ScreenCapture", "MediaProjection initialized")
    }

    suspend fun captureScreen(): Bitmap? = withContext(Dispatchers.IO) {
        if (mediaProjection == null || context == null) {
            MiniLogger.e("ScreenCapture", "MediaProjection not initialized")
            return@withContext null
        }

        val windowManager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        suspendCancellableCoroutine { continuation ->
            try {
                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "MiniScreenCapture",
                    width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null
                )

                imageReader?.setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage()
                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)
                        
                        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                        
                        image.close()
                        cleanupCaptureResources()
                        
                        if (continuation.isActive) {
                            continuation.resume(croppedBitmap)
                        }
                    }
                }, null)
            } catch (e: Exception) {
                MiniLogger.e("ScreenCapture", "Failed to capture screen", e)
                cleanupCaptureResources()
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }

    private fun cleanupCaptureResources() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    fun release() {
        cleanupCaptureResources()
        mediaProjection?.stop()
        mediaProjection = null
        context = null
        MiniLogger.i("ScreenCapture", "MediaProjection released")
    }
}
