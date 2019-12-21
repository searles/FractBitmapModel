package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractBitmapModel

interface BitmapModelChange: Change {
    fun accept(model: FractBitmapModel)
}