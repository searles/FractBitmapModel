package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.CalcController

interface ControllerChange: Change {
    fun accept(controller: CalcController)
}