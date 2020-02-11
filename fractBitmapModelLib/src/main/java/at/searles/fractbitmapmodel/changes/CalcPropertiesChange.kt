package at.searles.fractbitmapmodel.changes

interface CalcPropertiesChange: Change {
    fun accept(calcProperties: CalcProperties): CalcProperties
}