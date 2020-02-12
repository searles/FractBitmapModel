package at.searles.fractbitmapmodel

import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.searles.fractlang.ParameterEntry

/**
 * This class has 3 purposes:
 *
 * + Create CalcTasks (requires CalcScript, width, height, calcData from BitmapAllocation)
 * + Mediator between properties and this.
 * +
 * contains the scripts for calc. It also maintains
 *
 *
 */
class CalcController(val rs: RenderScript,
                     initialProperties: FractProperties) {

    private lateinit var calcScript: ScriptC_calc

    private var codeAllocation: Allocation = Allocation.createSized(rs, Element.I32(rs), 1)

    private val part = Allocation.createSized(rs, Element.F32_3(rs),
        parallelCalculationsCount
    )

    var properties = initialProperties

    fun initialize() {
        calcScript = ScriptC_calc(rs)
        updateVmCodeInScript()
    }

    fun createCalculationTask(bitmapAllocation: BitmapAllocation): CalcTask {
        return CalcTask(rs, bitmapAllocation, calcScript, part)
    }

    fun updateVmCodeInScript() {
        val vmCode = properties.vmCode

        codeAllocation.destroy()
        codeAllocation = Allocation.createSized(rs, Element.I32(rs), vmCode.size)

        codeAllocation.copyFrom(vmCode)

        calcScript.bind_code(codeAllocation)
        calcScript._codeSize = vmCode.size.toLong()
    }

    fun createScriptScale(width: Int, height: Int): ScriptField_Scale.Item {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val factor = 1.0 / if (centerX < centerY) centerX else centerY

        val scale = properties.scale

        return ScriptField_Scale.Item().apply {
            a = scale.xx * factor
            b = scale.yx * factor
            c = scale.xy * factor
            d = scale.yy * factor

            e = scale.cx - (a * centerX + b * centerY)
            f = scale.cy - (c * centerX + d * centerY)
        }
    }

    fun setScriptScale(scriptScale: ScriptField_Scale.Item) {
        calcScript._scale = scriptScale
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}