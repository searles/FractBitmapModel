package at.searles.fractbitmapmodel.tasks

import android.util.SparseArray
import androidx.core.util.set
import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.CalcProperties
import at.searles.fractlang.FractlangProgram

class SourceCodeChange(val sourceCode: String, val parameters: Map<String, String>): CalcChange, ControllerChange {

    private val fractlangProgram = FractlangProgram(sourceCode, parameters)

    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithNewSourceCode(fractlangProgram)
    }

    override fun accept(controller: CalcController) {
        val scale = CalcProperties.getScale(fractlangProgram.scale)
        val palettes = CalcProperties.getPalettes(fractlangProgram.palettes)
        controller.updateDefaultBitmapProperties(scale, palettes)
    }
}