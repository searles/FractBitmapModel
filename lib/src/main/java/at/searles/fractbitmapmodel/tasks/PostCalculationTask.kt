package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalculationTaskFactory

interface PostCalculationTask {
    val isParameterChange: Boolean
    fun execute(preferences: CalculationTaskFactory)
}