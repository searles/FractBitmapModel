package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import at.searles.fractbitmapmodel.CalculationTaskFactory

class RelativeScaleTask(private val relativeMatrix: Matrix): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(preferences: CalculationTaskFactory) {
        val scaledFractal = preferences.bitmapModelParameters.createWithRelativeScale(relativeMatrix)
        preferences.bitmapModelParameters = scaledFractal
    }
}