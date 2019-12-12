package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalculationTaskFactory

class ChangeBitmapTask(private val bitmapAllocation: BitmapAllocation): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(preferences: CalculationTaskFactory) {
        preferences.bitmapAllocation = bitmapAllocation
    }
}