package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalcController

class BitmapAllocationChange(private val newBitmapAllocation: BitmapAllocation): ControllerChange {
    override fun accept(controller: CalcController) {
        controller.bitmapAllocation = newBitmapAllocation
    }
}