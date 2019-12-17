package at.searles.fractbitmapmodel

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapmodel.tasks.BitmapModelParameters
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class CalculationTaskFactory(val rs: RenderScript, initialBitmapModelParameters: BitmapModelParameters, initialBitmapAllocation: BitmapAllocation) {

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)
    private val interpolateGapsScript = ScriptC_interpolate_gaps(rs)

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateBitmapInScripts()
            updateScaleInScripts()
        }

    var codeAllocation: Allocation = Allocation.createSized(rs, Element.I32(rs), 1)

    private val part = Allocation.createSized(rs, Element.F32_3(rs),
        parallelCalculationsCount
    )

    val width get() = bitmapAllocation.width
    val height get() = bitmapAllocation.height

    var minPixelGap: Int = 1 // use this to ensure a higher resolution.
    var pixelGap: Int = 0

    val bitmap: Bitmap
        get() = bitmapAllocation.bitmap

    var fractal = initialBitmapModelParameters
        set(value) {
            field = value
            updateVmCode()
            updateScaleInScripts()
            updatePalettesInScripts()
            updateLightParametersInScripts()
        }

    init {
        updateBitmapInScripts()
        updateVmCode()
        updateScaleInScripts()
        updatePalettesInScripts()
        updateLightParametersInScripts()
    }

    fun createCalculationTask(): CalculationTask {
        return CalculationTask(
            rs,
            width,
            height,
            bitmapAllocation.bitmapData,
            calcScript,
            part
        )
    }

    fun setPaletteOffset(index: Int, offsetX: Float, offsetY: Float) {
        PaletteUpdater(rs, bitmapScript)
            .updateOffsets(index, offsetX, offsetY)
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun syncBitmap() {
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
        with(bitmapAllocation) {
            bitmapScript.bind_bitmapData(bitmapData)
            calcScript._bitmapData = bitmapData

            calcScript._width = width.toLong()
            calcScript._height = height.toLong()

            bitmapScript._width = width.toLong()
            bitmapScript._height = height.toLong()

            interpolateGapsScript._width = width.toLong()
            interpolateGapsScript._height = height.toLong()
            // must call updateScaleInScripts afterwards!
        }
    }

    private fun updateLightParametersInScripts() {
        with(fractal) {
            bitmapScript._useLightEffect = if (shader3DProperties.useLightEffect) 1 else 0
            bitmapScript._lightVector = shader3DProperties.lightVector
            bitmapScript._ambientReflection = shader3DProperties.ambientReflection
            bitmapScript._diffuseReflection = shader3DProperties.diffuseReflection
            bitmapScript._specularReflection = shader3DProperties.specularReflection
            bitmapScript._shininess = shader3DProperties.shininess.toLong()
        }
    }

    private fun updateVmCode() {
        val vmCode = fractal.compilerInstance.vmCode

        codeAllocation.destroy()
        codeAllocation = Allocation.createSized(rs, Element.I32(rs), vmCode.size)

        codeAllocation.copyFrom(vmCode.toIntArray())

        calcScript.bind_code(codeAllocation)
        calcScript._codeSize = vmCode.size.toLong()
    }

    fun setLightVector(polarAngle: Float, azimuthAngle: Float) {
        bitmapScript._lightVector = Float3(
            sin(polarAngle) * sin(azimuthAngle),
            sin(polarAngle) * cos(azimuthAngle),
            -cos(polarAngle)
        )
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}