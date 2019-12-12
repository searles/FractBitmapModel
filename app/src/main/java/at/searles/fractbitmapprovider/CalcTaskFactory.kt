package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractbitmapprovider.palette.PaletteUpdater

class CalcTaskFactory(val rs: RenderScript, initialFractal: Fractal, initialBitmapAllocation: BitmapAllocation) {

    val calcScript: ScriptC_calc = ScriptC_calc(rs)
    val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)
    val interpolateGapsScript = ScriptC_interpolate_gaps(rs)

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateBitmapInScripts()
            updateScaleInScripts()
        }

    val width get() = bitmapAllocation.width
    val height get() = bitmapAllocation.height

    val bitmap: Bitmap
        get() = bitmapAllocation.bitmap

    var fractal = initialFractal
        set(value) {
            field = value
            updateScaleInScripts()
            updatePalettesInScripts()
        }

    var lightVector: Float3 = Float3(2f/3f, 2f/3f, 1f/3f)
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
    fun syncBitmap() {
        bitmapScript.forEach_root(bitmapAllocation.rsBitmap)
        bitmapAllocation.syncBitmap()
    }

    fun fastSyncBitmap(pixelGap: Int) {
        interpolateGapsScript._bitmap = bitmapAllocation.rsBitmap
        bitmapScript._stepSize = pixelGap.toLong()
        interpolateGapsScript._stepSize = pixelGap.toLong()

        bitmapScript.forEach_fastRoot(bitmapAllocation.rsBitmap)
        interpolateGapsScript.forEach_root(bitmapAllocation.rsBitmap)

        bitmapAllocation.syncBitmap()
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
        bitmapScript._lightVector = lightVector
        bitmapScript._ambientLight = 0.1f // FIXME extract
        bitmapScript._diffuseLight = 1f
        bitmapScript._specularStrength = 1f
        bitmapScript._viewerVector = Float3(1f / 3f, 2f / 3f, 2f / 3f)
        bitmapScript._shininess = 4
        bitmapScript._useLightEffect = 1
    }
}