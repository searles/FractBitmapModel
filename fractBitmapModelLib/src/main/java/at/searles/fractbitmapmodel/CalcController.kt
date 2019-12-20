package at.searles.fractbitmapmodel

import android.os.Looper
import android.renderscript.*
import at.searles.commons.math.Scale
import at.searles.paletteeditor.Palette

class CalcController(val rs: RenderScript,
                     firstCalcProperties: CalcProperties,
                     firstBitmapProperties: BitmapProperties,
                     firstBitmapAllocation: BitmapAllocation) {

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)

    val bitmapSync = BitmapSync(rs, firstBitmapProperties, firstBitmapAllocation)

    var bitmapProperties
        get() = bitmapSync.bitmapProperties
        set(value) {
            bitmapSync.bitmapProperties = value
        }

    var bitmapAllocation: BitmapAllocation = firstBitmapAllocation
        set(value) {
            require(Looper.getMainLooper().isCurrentThread)
            field = value
            bitmapSync.bitmapAllocation = value
            setBitmapAllocationInScript()
            setScaleInScript()
        }

    var codeAllocation: Allocation = Allocation.createSized(rs, Element.I32(rs), 1)

    private val part = Allocation.createSized(rs, Element.F32_3(rs),
        parallelCalculationsCount
    )

    val width get() = bitmapAllocation.width
    val height get() = bitmapAllocation.height
    val bitmap get() = bitmapAllocation.bitmap

    var calcProperties = firstCalcProperties
        set(value) {
            require(Looper.getMainLooper().isCurrentThread)
            field = value
            setVmCodeInScript()
            setScaleInScript()
        }

    init {
        setBitmapAllocationInScript()
        setVmCodeInScript()
        setScaleInScript()
    }

    internal fun createCalculationTask(): CalculationTask {
        return CalculationTask(rs, width, height, calcScript, part)
    }

    private fun setScaleInScript() {
        val scriptScale = createScriptScale(width, height, calcProperties.scale)
        calcScript._scale = scriptScale

        bitmapSync.setScaleInScripts(scriptScale)
    }

    private fun setBitmapAllocationInScript() {
        with(bitmapAllocation) {
            calcScript._calcData = calcData
            calcScript._width = width.toLong()
            calcScript._height = height.toLong()
        }
    }

    private fun setVmCodeInScript() {
        val vmCode = calcProperties.vmCode

        codeAllocation.destroy()
        codeAllocation = Allocation.createSized(rs, Element.I32(rs), vmCode.size)

        codeAllocation.copyFrom(vmCode)

        calcScript.bind_code(codeAllocation)
        calcScript._codeSize = vmCode.size.toLong()
    }

    fun updateBitmap() {
        require(Looper.getMainLooper().isCurrentThread)
        bitmapSync.updateBitmap()
    }

    /**
     * If the source code changed, then there might be a new default palette
     * or a new default scale.
     */
    fun updateDefaultPropertiesFromSource() {
        // TODO in the beginning not yet interesting.
    }

    companion object {
        const val parallelCalculationsCount = 8192

        fun createScriptScale(width: Int, height: Int, scale: Scale): ScriptField_Scale.Item {
            val centerX = width / 2.0
            val centerY = height / 2.0
            val factor = 1.0 / if (centerX < centerY) centerX else centerY

            return ScriptField_Scale.Item().apply {
                a = scale.xx * factor
                b = scale.yx * factor
                c = scale.xy * factor
                d = scale.yy * factor

                e = scale.cx - (a * centerX + b * centerY)
                f = scale.cy - (c * centerX + d * centerY)
            }
        }
    }
}