package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.graphics.Matrix
import at.searles.fractbitmapprovider.RenderScriptBitmapModel

class RelativeScaleTask(): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(model: RenderScriptBitmapModel) {
        val scaledFractal = model.fractal.createScaled(model.popImageTransformMatrix())
        model.fractal = scaledFractal
    }
}