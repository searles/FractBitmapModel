package at.searles.fractbitmapmodel.changes

import android.graphics.Matrix

class RelativeScaleChange(private val relativeMatrix: Matrix): CalcPropertiesChange {
    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithRelativeScale(relativeMatrix)
    }
}