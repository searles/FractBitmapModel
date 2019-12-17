package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.Shader3DProperties
import at.searles.fractlang.CompilerInstance
import at.searles.paletteeditor.Palette

class BitmapModelParameters(
    val scale: Scale,
    val palettes: List<Palette>,
    val shader3DProperties: Shader3DProperties,
    val compilerInstance: CompilerInstance) {

    // TODO light should be here too.

    val vmCode = compilerInstance.vmCode

    fun createScaled(m: Matrix): BitmapModelParameters {
        val mInverse = Matrix()
        m.invert(mInverse)
        return BitmapModelParameters(scale.createRelative(Scale.fromMatrix(*mInverse.values())), palettes, shader3DProperties, compilerInstance)
    }
}