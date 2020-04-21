package at.searles.fractbitmapmodel

import android.annotation.SuppressLint
import android.os.Handler
import android.renderscript.RenderScript
import kotlin.math.max

class BitmapController(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation
) {
    private var bitmapScript: ScriptC_bitmap? = null
    private var interpolateGapsScript: ScriptC_interpolate_gaps? = null
    private var paletteUpdater: PaletteToScriptUpdater? = null
    private val bitmapUpdater = BitmapUpdater()

    var listener: Listener? = null

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field.rsBitmap.destroy()
            field = value
            bindToBitmapAllocation()
        }

    fun initialize() {
        bitmapScript = ScriptC_bitmap(rs)
        interpolateGapsScript = ScriptC_interpolate_gaps(rs)
        paletteUpdater = PaletteToScriptUpdater(rs, bitmapScript!!)
        bindToBitmapAllocation()
    }

    private fun bindToBitmapAllocation() {
        if(bitmapScript == null) {
            // it will be initialized later.
            return
        }

        with(bitmapAllocation) {
            bitmapScript!!.bind_bitmapData(calcData)

            bitmapScript!!._width = width.toLong()
            bitmapScript!!._height = height.toLong()

            interpolateGapsScript!!._width = width.toLong()
            interpolateGapsScript!!._height = height.toLong()

            interpolateGapsScript!!._bitmap = rsBitmap
        }
    }

    fun scheduleBitmapUpdate() {
        bitmapUpdater.scheduleUpdate()
    }

    fun startAnimation(delayMs: Int, maxResolution: Int) {
        bitmapUpdater.minimumDelayMs = delayMs
        bitmapUpdater.maxAnimationResolution = maxResolution

        bitmapUpdater.isAnimation = true
        bitmapUpdater.scheduleUpdate()
    }

    fun stopAnimation() {
        bitmapUpdater.isAnimation = false
        // if the animation is running, an update is scheduled anyways.
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    private fun updateBitmap(isAnimation: Boolean, maxAnimationResolution: Int) {
        if(bitmapScript == null || interpolateGapsScript == null) {
            return
        }

        var currentPixelGap = bitmapAllocation.pixelGap

        if(isAnimation) {
            val size = max(bitmapAllocation.width, bitmapAllocation.height)

            while (size > maxAnimationResolution * currentPixelGap) {
                currentPixelGap *= 2
            }
        }

        bitmapScript!!._pixelGap = currentPixelGap.toLong()

        if(currentPixelGap == 1) {
            if(isAnimation) {
                bitmapScript!!.forEach_fastRoot(bitmapAllocation.rsBitmap)
            } else {
                bitmapScript!!.forEach_root(bitmapAllocation.rsBitmap)
            }
        } else {
            interpolateGapsScript!!._pixelGap = currentPixelGap.toLong()

            bitmapScript!!.forEach_fastRoot(bitmapAllocation.rsBitmap)
            interpolateGapsScript!!.forEach_root(bitmapAllocation.rsBitmap)
        }

        bitmapAllocation.sync()
    }

    fun updatePalettes(props: FractProperties) {
        if(paletteUpdater == null) {
            return
        }

        paletteUpdater!!.updatePalettes(props)
    }

    fun updateShaderProperties(props: FractProperties) {
        if(bitmapScript == null) {
            return
        }

        with(props) {
            bitmapScript!!._useLightEffect = if (shaderProperties.useLightEffect) 1 else 0
            bitmapScript!!._lightVector = shaderProperties.lightVector
            bitmapScript!!._ambientReflection = shaderProperties.ambientReflection
            bitmapScript!!._diffuseReflection = shaderProperties.diffuseReflection
            bitmapScript!!._specularReflection = shaderProperties.specularReflection
            bitmapScript!!._shininess = shaderProperties.shininess.toLong()
        }
    }

    interface Listener {
        fun bitmapUpdated()
    }

    private inner class BitmapUpdater {
        var maxAnimationResolution: Int = -1
        var minimumDelayMs: Int = -1

        var isAnimation: Boolean = false

        var isUpdateScheduled: Boolean = false
        var lastUpdateTimestamp: Long = -1
        var handler: Handler = Handler()

        fun scheduleUpdate() {
            if(isUpdateScheduled) {
                return
            }

            isUpdateScheduled = true
            updateDelayed()
        }

        fun updateDelayed() {
            val delay = minimumDelayMs - (System.currentTimeMillis() - lastUpdateTimestamp)
            handler.postDelayed({createUpdateTask().run()}, max(1L, delay))
        }

        private fun createUpdateTask(): Runnable {
            return UpdateTask(isAnimation, maxAnimationResolution)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class UpdateTask(private val isAnimation: Boolean, private val maxAnimationResolution: Int): Runnable {
        override fun run() {
            updateBitmap(isAnimation, maxAnimationResolution)
            bitmapUpdater.lastUpdateTimestamp = System.currentTimeMillis()

            if(isAnimation) {
                bitmapUpdater.updateDelayed()
            } else {
                bitmapUpdater.isUpdateScheduled = false
            }

            listener?.bitmapUpdated()
        }
    }
}