package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalcController

interface ControllerChange {
    fun accept(controller: CalcController)
}