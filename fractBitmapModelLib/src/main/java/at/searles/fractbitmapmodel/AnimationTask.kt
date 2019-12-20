package at.searles.fractbitmapmodel

import android.os.Handler
import kotlin.math.sin

class AnimationTask(private val controller: CalcController) {
    val handler = Handler()
    private var isCancelled: Boolean = false
    private var minPixelGap = 4

    private var alpha: Float = 0f

    private val bitmapSync = controller.bitmapSync

    private fun step() {
        bitmapSync.setLightVector(sin(0.782 * alpha) * 1.5, alpha.toDouble())
        bitmapSync.setPaletteOffset(0, alpha * 0.17f, alpha * 0.03f)
        alpha += 0.05f
        controller.updateBitmap()
    }

    fun start() {
        bitmapSync.minPixelGap = minPixelGap

        val task = object: Runnable {
            override fun run() {
                if (isCancelled) {
                    return
                }

                step()

                handler.postDelayed(this, delayMillis)

            }
        }

        handler.postDelayed(task, delayMillis)
    }

    fun cancel() {
        isCancelled = true
    }

    companion object {
        const val delayMillis = 40L
    }
}