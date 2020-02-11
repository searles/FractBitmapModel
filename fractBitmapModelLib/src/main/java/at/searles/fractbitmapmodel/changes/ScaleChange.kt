package at.searles.fractbitmapmodel.changes

import at.searles.commons.math.Scale

class ScaleChange(private val scale: Scale): CalcPropertiesChange {
    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithNewScale(scale)
    }
}