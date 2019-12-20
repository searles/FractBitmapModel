package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.CalcProperties

interface CalcPropertiesChange: Change {
    fun accept(calcProperties: CalcProperties): CalcProperties
}