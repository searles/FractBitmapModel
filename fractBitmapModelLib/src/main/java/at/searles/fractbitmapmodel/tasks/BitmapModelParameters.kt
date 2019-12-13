package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.paletteeditor.Palette

class BitmapModelParameters(val scale: Scale, val palettes: List<Palette>) {
    fun createScaled(m: Matrix): BitmapModelParameters {
        val mInverse = Matrix()
        m.invert(mInverse)
        return BitmapModelParameters(scale.createRelative(Scale.fromMatrix(*mInverse.values())), palettes)
    }
}