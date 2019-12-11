package at.searles.fractbitmapprovider.fractalbitmapmodel

import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.RenderScriptBitmapModel

class ChangeBitmapTask(private val bitmapAllocation: BitmapAllocation): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(model: RenderScriptBitmapModel) {
        model.bitmapAllocation = bitmapAllocation
    }
}