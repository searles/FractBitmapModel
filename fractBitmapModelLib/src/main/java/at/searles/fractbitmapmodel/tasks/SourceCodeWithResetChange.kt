package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.BitmapProperties
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties
import at.searles.fractlang.FractlangProgram

/**
 * Use this one to load a demo. Existing settings are dropped apart from
 * shader properties.
 */
class SourceCodeWithResetChange(newSourceCode: String, newParameters: Map<String, String>): CalcPropertiesChange, ControllerChange {

    private val fractlangProgram = FractlangProgram(newSourceCode, newParameters)
    private val newCalcProperties: CalcProperties

    init {
        val scale = CalcProperties.getScale(fractlangProgram.scale)
        newCalcProperties = CalcProperties(scale, fractlangProgram)
    }

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return newCalcProperties
    }

    override fun accept(controller: CalcController) {
        val palettes = CalcProperties.getPalettes(fractlangProgram.palettes)
        val shaderProperties = controller.bitmapProperties.shaderProperties

        controller.bitmapProperties = BitmapProperties(palettes, shaderProperties)

        // TODO: Change scale or palette if they are default in the controller.
        controller.updateDefaultPropertiesFromSource()
    }
}