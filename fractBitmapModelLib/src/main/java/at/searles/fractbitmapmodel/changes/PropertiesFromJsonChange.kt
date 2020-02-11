package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractBitmapModel
import org.json.JSONObject

/**
 * Use this one to load favorites
 */
class PropertiesFromJsonChange(obj: JSONObject): CalcPropertiesChange, BitmapModelChange {

    private val newCalcProperties = CalcProperties.fromJson(obj)
    private val newBitmapProperties = BitmapProperties.fromJson(obj)

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return newCalcProperties
    }

    override fun accept(model: FractBitmapModel) {
        model.setBitmapProperties(newBitmapProperties)
    }
}