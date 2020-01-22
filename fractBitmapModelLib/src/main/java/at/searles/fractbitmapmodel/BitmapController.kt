package at.searles.fractbitmapmodel

import android.renderscript.RenderScript
import at.searles.paletteeditor.Palette
import kotlin.math.hypot
import kotlin.math.max

class BitmapController(
    val rs: RenderScript,
    initBitmapProperties: BitmapProperties,
    initialBitmapAllocation: BitmapAllocation
) {

    private lateinit var bitmapScript: ScriptC_bitmap
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

    var bitmapProperties: BitmapProperties = initBitmapProperties
        set(value) {
            field = value
            setPalettesInScripts()
            setShaderPropertiesInScripts()
        }

    fun initialize() {
        bitmapScript = ScriptC_bitmap(rs)
        interpolateGapsScript = ScriptC_interpolate_gaps(rs)
        paletteUpdater = PaletteToScriptUpdater(rs, bitmapScript)
        setPalettesInScripts()
        setShaderPropertiesInScripts()
        bindToBitmapAllocation()
    }

    var shaderProperties
        get() = bitmapProperties.shaderProperties
        set(value) {
            bitmapProperties = BitmapProperties(palettes, value)
            setShaderPropertiesInScripts()
        }

    var palettes: List<Palette>
        get() = bitmapProperties.palettes
        set(value) {
            bitmapProperties = BitmapProperties(value, shaderProperties)
            setPalettesInScripts()
        }

    private fun bindToBitmapAllocation() {
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
    fun updateBitmap() {
        val currentPixelGap = max(minPixelGap, bitmapAllocation.pixelGap)

        if(currentPixelGap == 1) {
            bitmapScript.forEach_root(bitmapAllocation.rsBitmap)
        } else {
            bitmapScript._pixelGap = currentPixelGap.toLong()
            interpolateGapsScript._pixelGap = currentPixelGap.toLong()

            bitmapScript.forEach_fastRoot(bitmapAllocation.rsBitmap)
            interpolateGapsScript.forEach_root(bitmapAllocation.rsBitmap)
        }

        bitmapAllocation.sync()
        listener?.bitmapUpdated()
    }

    private fun setPalettesInScripts() {
        paletteUpdater.updatePalettes(bitmapProperties.palettes)
    }

    private fun setShaderPropertiesInScripts() {
        with(bitmapProperties) {
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