package at.searles.fractbitmapmodel

import android.os.Handler
import kotlin.math.sin

class AnimationTask(private val calculationTaskFactory: CalculationTaskFactory) {
    val handler = Handler()
    private var isCancelled: Boolean = false
    private var minPixelGap = 4

    private var alpha: Float = 0f

    private fun step() {
        calculationTaskFactory.setLightVector(sin(0.782f * alpha) * 1.5f, alpha)
        calculationTaskFactory.setPaletteOffset(0, alpha * 0.17f, alpha * 0.03f)

        alpha += 0.05f

        val startTimeStamp = System.currentTimeMillis()
        calculationTaskFactory.syncBitmap()
        val duration = System.currentTimeMillis() - startTimeStamp

        if(minPixelGap > 1 && duration * 3 < delayMillis) {
            minPixelGap /= 2
            calculationTaskFactory.minPixelGap = minPixelGap
        } else if(minPixelGap < 64 && delayMillis * 4 < duration) {
            minPixelGap *= 2
            calculationTaskFactory.minPixelGap = minPixelGap
        }
    }

    fun start() {
        calculationTaskFactory.minPixelGap = minPixelGap

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