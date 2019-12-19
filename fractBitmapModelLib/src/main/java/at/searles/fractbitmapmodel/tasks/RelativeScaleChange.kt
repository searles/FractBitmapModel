package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties

class RelativeScaleChange(private val relativeMatrix: Matrix): CalcChange {
    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithRelativeScale(relativeMatrix)
    }
}