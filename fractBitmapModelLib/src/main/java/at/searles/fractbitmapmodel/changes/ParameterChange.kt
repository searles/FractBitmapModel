package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException

class ParameterChange(private val key: String, private val newValue: String): CalcPropertiesChange {
    /**
     * @throws SemanticAnalysisException if it causes a compiler error.
     */
    override fun accept(properties: FractProperties): FractProperties {
        return properties.createWithNewParameter(key, newValue)
    }
}