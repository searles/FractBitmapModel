package at.searles.fractbitmapprovider.fractalbitmapmodel

import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.CalcTaskFactory

class ChangeBitmapTask(private val bitmapAllocation: BitmapAllocation): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(preferences: CalcTaskFactory) {
        preferences.bitmapAllocation = bitmapAllocation
    }
}