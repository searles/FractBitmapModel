package at.searles.fractbitmapprovider.fractalbitmapmodel

import at.searles.fractbitmapprovider.CalcTaskFactory

interface PostCalculationTask {
    val isParameterChange: Boolean
    fun execute(preferences: CalcTaskFactory)
}