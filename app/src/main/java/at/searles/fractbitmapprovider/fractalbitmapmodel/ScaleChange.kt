package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.graphics.Matrix

class ScaleChange(private val scaleMatrix: () -> Matrix): CalculationChange {
    override fun applyChange(original: Fractal): Fractal {
        return original.createScaled(scaleMatrix.invoke())
    }
}