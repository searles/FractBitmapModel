package at.searles.fractbitmapmodel.changes

import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.CalcProperties

class ScaleChange(private val scale: Scale): CalcPropertiesChange {
    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithNewScale(scale)
    }
}