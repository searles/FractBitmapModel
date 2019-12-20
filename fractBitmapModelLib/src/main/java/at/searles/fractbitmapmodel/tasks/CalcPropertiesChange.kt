package at.searles.fractbitmapmodel.tasks

import at.searles.fractbitmapmodel.CalcProperties

interface CalcPropertiesChange: Change {
    fun accept(calcProperties: CalcProperties): CalcProperties
}