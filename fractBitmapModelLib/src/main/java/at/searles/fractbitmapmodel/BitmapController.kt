package at.searles.fractbitmapmodel

import android.renderscript.RenderScript
import kotlin.math.max

class BitmapController(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation
) {
    private var bitmapScript: ScriptC_bitmap? = null
    private var interpolateGapsScript: ScriptC_interpolate_gaps? = null
    private var paletteUpdater: PaletteToScriptUpdater? = null

    var minPixelGap: Int = 1 // use this to ensure a lower but faster resolution.
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

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun updateBitmap(alwaysUseFastIfPossible: Boolean) {
        if(bitmapScript == null || interpolateGapsScript == null) {
            return
        }

        val currentPixelGap = max(minPixelGap, bitmapAllocation.pixelGap)
        bitmapScript!!._pixelGap = currentPixelGap.toLong()

        if(currentPixelGap == 1) {
            if(alwaysUseFastIfPossible) {
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
        listener?.bitmapUpdated()
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
}