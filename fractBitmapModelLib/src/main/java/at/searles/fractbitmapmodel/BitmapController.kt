package at.searles.fractbitmapmodel

import android.renderscript.RenderScript
import kotlin.math.hypot
import kotlin.math.max

class BitmapController(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation
) {
    lateinit var bitmapScript: ScriptC_bitmap
    
    private var isInitialized: Boolean = false
    
    private lateinit var interpolateGapsScript: ScriptC_interpolate_gaps

    private lateinit var paletteUpdater: PaletteToScriptUpdater

    var minPixelGap: Int = 1 // use this to ensure a lower but faster resolution.
    var listener: Listener? = null

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field.rsBitmap.destroy()
            field = value
            bindToBitmapAllocation()
        }

    fun initialize(props: FractProperties) {
        if(!isInitialized) {
            bitmapScript = ScriptC_bitmap(rs)
            interpolateGapsScript = ScriptC_interpolate_gaps(rs)
            paletteUpdater = PaletteToScriptUpdater(rs, bitmapScript)
            isInitialized = true
            updatePalettes(props)
            updateShaderProperties(props)
            bindToBitmapAllocation()
        }
    }

    private fun bindToBitmapAllocation() {
        require(isInitialized)
        
        with(bitmapAllocation) {
            bitmapScript.bind_bitmapData(calcData)

            bitmapScript._width = width.toLong()
            bitmapScript._height = height.toLong()

            interpolateGapsScript._width = width.toLong()
            interpolateGapsScript._height = height.toLong()

            interpolateGapsScript._bitmap = rsBitmap
        }
    }

    fun setScriptScale(scaleInScript: ScriptField_Scale.Item) {
        require(isInitialized)

        bitmapScript._scale = scaleInScript

        val xStepLength = hypot(scaleInScript.a, scaleInScript.c)
        val yStepLength = hypot(scaleInScript.b, scaleInScript.d)

        bitmapScript._xStepLength = xStepLength
        bitmapScript._yStepLength = yStepLength

        bitmapScript._aNorm = (scaleInScript.a / xStepLength).toFloat()
        bitmapScript._bNorm = (scaleInScript.b / yStepLength).toFloat()
        bitmapScript._cNorm = (scaleInScript.c / xStepLength).toFloat()
        bitmapScript._dNorm = (scaleInScript.d / yStepLength).toFloat()
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun updateBitmap(alwaysUseFast: Boolean = false) {
        if(!isInitialized) {
            return
        }

        val currentPixelGap = max(minPixelGap, bitmapAllocation.pixelGap)
        bitmapScript._pixelGap = currentPixelGap.toLong()

        if(currentPixelGap == 1) {
            if(alwaysUseFast) {
                bitmapScript.forEach_fastRoot(bitmapAllocation.rsBitmap)
            } else {
                bitmapScript.forEach_root(bitmapAllocation.rsBitmap)
            }
        } else {
            interpolateGapsScript._pixelGap = currentPixelGap.toLong()

            bitmapScript.forEach_fastRoot(bitmapAllocation.rsBitmap)
            interpolateGapsScript.forEach_root(bitmapAllocation.rsBitmap)
        }

        bitmapAllocation.sync()
        listener?.bitmapUpdated()
    }

    fun updatePalettes(props: FractProperties) {
        if(!isInitialized) {
            return
        }

        paletteUpdater.updatePalettes(props)
    }

    fun updateShaderProperties(props: FractProperties) {
        if(!isInitialized) {
            return 
        }
        
        with(props) {
            bitmapScript._useLightEffect = if (shaderProperties.useLightEffect) 1 else 0
            bitmapScript._lightVector = shaderProperties.lightVector
            bitmapScript._ambientReflection = shaderProperties.ambientReflection
            bitmapScript._diffuseReflection = shaderProperties.diffuseReflection
            bitmapScript._specularReflection = shaderProperties.specularReflection
            bitmapScript._shininess = shaderProperties.shininess.toLong()
        }
    }

    interface Listener {
        fun bitmapUpdated()
    }
}