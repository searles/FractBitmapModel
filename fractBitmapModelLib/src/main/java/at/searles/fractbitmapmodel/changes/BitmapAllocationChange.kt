package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalcBitmapModel
import at.searles.fractbitmapmodel.CalcController

class BitmapAllocationChange(private val newBitmapAllocation: BitmapAllocation): BitmapModelChange {
    override fun accept(model: CalcBitmapModel) {
        model.bitmapAllocation = newBitmapAllocation
    }
}