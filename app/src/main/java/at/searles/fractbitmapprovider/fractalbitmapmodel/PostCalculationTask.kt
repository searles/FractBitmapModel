package at.searles.fractbitmapprovider.fractalbitmapmodel

import at.searles.fractbitmapprovider.RenderScriptBitmapModel

interface PostCalculationTask {
    val isParameterChange: Boolean
    fun execute(model: RenderScriptBitmapModel)
}