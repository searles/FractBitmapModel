package at.searles.fractbitmapmodel

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript

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

    lateinit var calcScript: ScriptC_calc

    private var isInitialized: Boolean = false

    private var codeAllocation: Allocation = Allocation.createSized(rs, Element.I32(rs), 1)

    var properties = initialProperties

    fun initialize() {
        if(!isInitialized) {
            calcScript = ScriptC_calc(rs)
            isInitialized = true
            updateVmCodeInScript()
        }
    }

    fun updateVmCodeInScript() {
        if(!isInitialized) {
            return
        }

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
        require(isInitialized)
        calcScript._scale = scriptScale
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}