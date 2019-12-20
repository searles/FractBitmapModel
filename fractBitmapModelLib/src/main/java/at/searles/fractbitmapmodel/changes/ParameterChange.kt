package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.CalcProperties

class ParameterChange(private val key: String, private val newValue: String): CalcPropertiesChange {
    /**
     * @throws SemanticAnalysisException if it causes a compiler error.
     */
    override fun accept(calcProperties: CalcProperties): CalcProperties {
        return calcProperties.createWithNewParameter(key, newValue)
    }
}