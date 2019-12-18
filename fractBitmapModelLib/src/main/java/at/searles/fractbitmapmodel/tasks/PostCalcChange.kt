package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalcController

interface PostCalcChange {
    fun accept(controller: CalcController)
}