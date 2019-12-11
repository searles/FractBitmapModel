package at.searles.fractbitmapprovider.fractalbitmapmodel

interface CalculationChange {
    fun applyChange(original: Fractal): Fractal
}