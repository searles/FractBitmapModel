package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.BitmapProperties
import at.searles.fractbitmapmodel.CalcBitmapModel
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties

/**
 * Use this one to load a demo. Existing settings are dropped apart from
 * shader properties.
 */
class PropertiesChange(private val newCalcProperties: CalcProperties, private val newBitmapProperties: BitmapProperties): CalcPropertiesChange, BitmapModelChange {

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return newCalcProperties
    }

    override fun accept(model: CalcBitmapModel) {
        model.setBitmapProperties(newBitmapProperties)
    }
}