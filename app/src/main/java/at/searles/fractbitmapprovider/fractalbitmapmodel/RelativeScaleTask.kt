package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.graphics.Matrix
import at.searles.fractbitmapprovider.CalcTaskFactory
import at.searles.fractbitmapprovider.TaskBitmapModel

class RelativeScaleTask(private val relativeMatrix: Matrix): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(preferences: CalcTaskFactory) {
        val scaledFractal = preferences.fractal.createScaled(relativeMatrix)
        preferences.fractal = scaledFractal
    }
}