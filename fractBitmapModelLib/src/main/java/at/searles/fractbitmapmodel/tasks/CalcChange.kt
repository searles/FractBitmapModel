package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalcProperties

interface CalcChange {
    fun accept(calcProperties: CalcProperties): CalcProperties
}