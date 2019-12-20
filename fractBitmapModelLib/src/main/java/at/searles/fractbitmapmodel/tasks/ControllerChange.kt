package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalcController

interface ControllerChange: Change {
    fun accept(controller: CalcController)
}