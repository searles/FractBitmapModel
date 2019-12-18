package at.searles.fractbitmapmodel

import android.renderscript.RenderScript
import kotlin.math.hypot
import kotlin.math.max

class BitmapSync(val rs: RenderScript, initBitmapProperties: BitmapProperties, val initBitmapAllocation: BitmapAllocation) {

    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)
    private val interpolateGapsScript = ScriptC_interpolate_gaps(rs)

    private val paletteUpdater = PaletteUpdater(rs, bitmapScript)

    var bitmapAllocation: BitmapAllocation = initBitmapAllocation
        set(value) {
            field = value
            setBitmapAllocationInScripts()
        }

    var minPixelGap: Int = 1 // use this to ensure a lower but faster resolution.
    var pixelGap: Int = 0

    var listener: Listener? = null

    var bitmapProperties: BitmapProperties = initBitmapProperties

    init {
        setPalettesInScripts()
        setLightParametersInScripts()
        setBitmapAllocationInScripts()
    }

    fun setPaletteOffset(index: Int, offsetX: Float, offsetY: Float) {
        paletteUpdater.updateOffsets(index, offsetX, offsetY)
    }

    fun setLightVector(polarAngle: Float, azimuthAngle: Float) {
        bitmapScript._lightVector = ShaderProperties.toVector(polarAngle, azimuthAngle)
    }

    fun setScaleInScripts(scaleInScript: ScriptField_Scale.Item) {
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
        val currentPixelGap = max(minPixelGap, pixelGap)

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

    private fun setBitmapAllocationInScripts() {
        with(bitmapAllocation) {
            bitmapScript.bind_bitmapData(calcData)

            bitmapScript._width = width.toLong()
            bitmapScript._height = height.toLong()

            interpolateGapsScript._width = width.toLong()
            interpolateGapsScript._height = height.toLong()

            interpolateGapsScript._bitmap = rsBitmap
        }
    }

    private fun setPalettesInScripts() {
        paletteUpdater.updatePalettes(bitmapProperties.palettes)
    }

    private fun setLightParametersInScripts() {
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