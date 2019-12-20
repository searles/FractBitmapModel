package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties
import at.searles.fractlang.FractlangProgram

/**
 * Use this one if the source code and/or parameters change
 * but everything else remains the same. Parameters
 * are either the existing ones or empty ones.
 * All other parameters, eg palette or scale, are kept.
 */
class FractlangChange(private val fractlangProgram: FractlangProgram): CalcPropertiesChange, ControllerChange {

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithNewSourceCode(fractlangProgram)
    }

    override fun accept(controller: CalcController) {
        val scale = CalcProperties.getScale(fractlangProgram.scale)
        val palettes = CalcProperties.getPalettes(fractlangProgram.palettes)

        // TODO: Change scale or palette if they are default in the controller.
        controller.updateDefaultPropertiesFromSource()
    }
}