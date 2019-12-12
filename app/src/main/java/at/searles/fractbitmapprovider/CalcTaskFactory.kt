package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractbitmapprovider.palette.PaletteUpdater
import kotlin.math.max

class CalcTaskFactory(val rs: RenderScript, initialFractal: Fractal, initialBitmapAllocation: BitmapAllocation) {

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)
    private val interpolateGapsScript = ScriptC_interpolate_gaps(rs)

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateBitmapInScripts()
            updateScaleInScripts()
        }

    val width get() = bitmapAllocation.width
    val height get() = bitmapAllocation.height

    var pixelGap: Int = 1

    val bitmap: Bitmap
        get() = bitmapAllocation.bitmap

    var fractal = initialFractal
        set(value) {
            field = value
            updateScaleInScripts()
            updatePalettesInScripts()
        }

    var shader3DProperties = Shader3DProperties()
        set(value) {
            field = value
            updateLightParametersInScripts()
        }

    init {
        updateBitmapInScripts()
        updateScaleInScripts()
        updatePalettesInScripts()
        updateLightParametersInScripts()
    }

    fun createCalculationTask(): CalculationTask {
        return CalculationTask(rs, width, height, bitmapAllocation.bitmapData, calcScript)
    }

    fun setPaletteOffset(index: Int, offsetX: Float, offsetY: Float) {
        PaletteUpdater(rs, bitmapScript).updateOffsets(index, offsetX, offsetY)
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun syncBitmap(minPixelGap: Int = pixelGap) {
        val currentPixelGap = max(minPixelGap, pixelGap)

        if(currentPixelGap == 1) {
            bitmapScript.forEach_root(bitmapAllocation.rsBitmap)
            bitmapAllocation.syncBitmap()
        } else {
            interpolateGapsScript._bitmap = bitmapAllocation.rsBitmap
            bitmapScript._pixelGap = currentPixelGap.toLong()
            interpolateGapsScript._pixelGap = currentPixelGap.toLong()

            bitmapScript.forEach_fastRoot(bitmapAllocation.rsBitmap)
            interpolateGapsScript.forEach_root(bitmapAllocation.rsBitmap)

            bitmapAllocation.syncBitmap()
        }
    }

    private fun updatePalettesInScripts() {
        PaletteUpdater(rs, bitmapScript).apply {
            this.updatePalettes(fractal.palettes)
        }
    }

    private fun updateScaleInScripts() {
        ScaleUpdater.updateScaleInScripts(width, height, fractal.scale, calcScript, bitmapScript)
    }

    private fun updateBitmapInScripts() {
        this.bitmapScript.bind_bitmapData(bitmapAllocation.bitmapData)

        this.calcScript._width = bitmapAllocation.width.toLong()
        this.calcScript._height = bitmapAllocation.height.toLong()

        this.bitmapScript._width = bitmapAllocation.width.toLong()
        this.bitmapScript._height = bitmapAllocation.height.toLong()

        this.interpolateGapsScript._width = bitmapAllocation.width.toLong()
        this.interpolateGapsScript._height = bitmapAllocation.height.toLong()
        // must call updateScaleInScripts afterwards!
    }

    private fun updateLightParametersInScripts() {
        bitmapScript._useLightEffect = if(shader3DProperties.useLightEffect) 1 else 0
        bitmapScript._lightVector = shader3DProperties.lightVector
        bitmapScript._ambientReflection = shader3DProperties.ambientReflection
        bitmapScript._diffuseReflection = shader3DProperties.diffuseReflection
        bitmapScript._specularReflection = shader3DProperties.specularReflection
        bitmapScript._shininess = shader3DProperties.shininess.toLong()
    }
}