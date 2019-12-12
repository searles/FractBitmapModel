package at.searles.fractbitmapmodel

import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.ScriptC_bitmap
import at.searles.fractbitmapmodel.ScriptC_calc
import at.searles.fractbitmapmodel.ScriptField_Scale
import kotlin.math.hypot

internal object ScaleUpdater {
    fun updateScaleInScripts(width: Int, height: Int, scale: Scale, calcScript: ScriptC_calc, bitmapScript: ScriptC_bitmap) {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val factor = 1.0 / if (centerX < centerY) centerX else centerY

        val scaleInScript = ScriptField_Scale.Item().apply {
            a = scale.xx * factor
            b = scale.yx * factor
            c = scale.xy * factor
            d = scale.yy * factor

            e = scale.cx - (a * centerX + b * centerY)
            f = scale.cy - (c * centerX + d * centerY)
        }

        calcScript._scale = scaleInScript

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
}