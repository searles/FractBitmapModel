package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.BitmapProperties
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties

/**
 * Use this one to load a demo. Existing settings are dropped apart from
 * shader properties.
 */
class PropertiesChange(private val newCalcProperties: CalcProperties, private val newBitmapProperties: BitmapProperties): CalcPropertiesChange, ControllerChange {

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return newCalcProperties
    }

    override fun accept(controller: CalcController) {
        controller.bitmapProperties = newBitmapProperties
        controller.updateDefaultPropertiesFromSource()
    }
}