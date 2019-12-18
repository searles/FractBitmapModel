package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import at.searles.fractbitmapmodel.CalcController

class RelativeScaleChange(private val relativeMatrix: Matrix): PostCalcChange {
    override fun accept(controller: CalcController) {
        controller.calcProperties = controller.calcProperties.createWithRelativeScale(relativeMatrix)
    }
}