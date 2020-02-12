package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractlang.FractlangProgram

class FractlangWithDefaultsChange(program: FractlangProgram): CalcPropertiesChange {

    private val newProperties: FractProperties = FractProperties.create(program, null, null, emptyList())

    override fun accept(properties: FractProperties): FractProperties {
        return newProperties
    }
}