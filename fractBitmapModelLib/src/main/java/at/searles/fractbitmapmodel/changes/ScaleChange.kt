package at.searles.fractbitmapmodel.changes

import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.FractProperties

class ScaleChange(private val scale: Scale): CalcPropertiesChange {
    override fun accept(calcProperties: FractProperties): FractProperties {
        return calcProperties.createWithNewScale(scale)
    }
}