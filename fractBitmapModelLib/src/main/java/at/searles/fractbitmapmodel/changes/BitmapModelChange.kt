package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.CalcBitmapModel

interface BitmapModelChange: Change {
    fun accept(model: CalcBitmapModel)
}