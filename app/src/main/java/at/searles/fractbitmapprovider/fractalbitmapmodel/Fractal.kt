package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.paletteeditor.Palette

class Fractal(val scale: Scale, val palettes: List<Palette>) {
    fun createScaled(m: Matrix): Fractal {
        val mInverse = Matrix()
        m.invert(mInverse)
        return Fractal(scale.createRelative(Scale.fromMatrix(*mInverse.values())), palettes)
    }
}