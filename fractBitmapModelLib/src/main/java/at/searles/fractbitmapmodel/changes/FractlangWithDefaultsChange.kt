package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.BitmapProperties
import at.searles.fractbitmapmodel.CalcBitmapModel
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties
import at.searles.fractlang.FractlangProgram

/**
 * Use this one to load a demo. Existing settings are dropped apart from
 * shader properties.
 */
class FractlangWithDefaultsChange(private val fractlangProgram: FractlangProgram): CalcPropertiesChange, BitmapModelChange {

    private val newCalcProperties: CalcProperties

    init {
        val scale = CalcProperties.getScale(fractlangProgram.scale)
        newCalcProperties = CalcProperties(scale, fractlangProgram)
    }

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return newCalcProperties
    }

    override fun accept(model: CalcBitmapModel) {
        val palettes = CalcProperties.getPalettes(fractlangProgram.palettes)
        model.setPalettes(palettes)
    }
}