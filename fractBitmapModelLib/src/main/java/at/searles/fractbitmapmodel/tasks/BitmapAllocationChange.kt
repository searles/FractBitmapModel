package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalcController

class BitmapAllocationChange(private val newBitmapAllocation: BitmapAllocation): ControllerChange {
    override fun accept(controller: CalcController) {
        controller.bitmapAllocation = newBitmapAllocation
    }
}