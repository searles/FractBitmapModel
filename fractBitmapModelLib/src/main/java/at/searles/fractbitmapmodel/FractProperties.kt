package at.searles.fractbitmapmodel

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.PaletteEntry
import at.searles.fractlang.ParameterEntry

class FractProperties(
    val sourceCode: String,
    private val customScale: Scale?,
    private val customShaderProperties: ShaderProperties?,
    private val customPalettes: List<Palette?>,
    private val defaultScale: Scale,
    private val defaultShaderProperties: ShaderProperties,
    private val defaultPalettes: List<PaletteEntry>,
    private val parameters: Map<String, ParameterEntry>,
    val vmCode: IntArray
) {
    val scale = customScale ?: defaultScale
    val isDefaultScale = customScale == null

    val shaderProperties = customShaderProperties ?: defaultShaderProperties
    val isDefaultShaderProperties = customShaderProperties == null

    val paletteCount: Int = defaultPalettes.size

    fun getPalette(index: Int): Palette {
        require(index in 0 until paletteCount)
        return customPalettes.getOrNull(index) ?: defaultPalettes[index].defaultPalette
    }

    fun isDefaultPalette(index: Int): Boolean {
        require(index in 0 until paletteCount)
        return customPalettes[index] == null
    }

    fun getPaletteDescription(index: Int): String {
        require(index in 0 until paletteCount)
        return defaultPalettes[index].description
    }

    val parameterIds: Iterable<String> = parameters.keys

    fun getParameterDescription(id: String): String {
        return parameters.getValue(id).description
    }

    fun getParameter(id: String): String {
        return parameters.getValue(id).expr
    }

    fun isDefaultParameter(id: String): Boolean {
        return parameters.getValue(id).isDefault
    }

    val customParameters: Map<String, String> by lazy {
        parameters.values.filter { !it.isDefault }.map { it.id to it.expr }.toMap()
    }

    fun createWithRelativeScale(relativeMatrix: Matrix): FractProperties {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return FractProperties(sourceCode, newScale, customShaderProperties, customPalettes, defaultScale, defaultShaderProperties, defaultPalettes, parameters, vmCode)
    }

    fun createWithNewScale(newScale: Scale): FractProperties {
        return FractProperties(sourceCode, newScale, customShaderProperties, customPalettes, defaultScale, defaultShaderProperties, defaultPalettes, parameters, vmCode)
    }

    fun createWithNewProgram(newFractlangProgram: FractlangProgram): FractProperties {
        return create(newFractlangProgram, customScale, customShaderProperties, customPalettes)
    }

    fun createWithResetParameter(parameterKey: String): FractProperties {
        val newParameters = customParameters.toMutableMap().apply {
            remove(parameterKey)
        }

        return create(sourceCode, newParameters, customScale, customShaderProperties, customPalettes)
    }

    fun createWithNewParameter(parameterKey: String, value: String): FractProperties {
        val newParameters = customParameters.toMutableMap().apply {
            this[parameterKey] = value
        }

        return create(sourceCode, newParameters, customScale, customShaderProperties, customPalettes)
    }


    companion object {
        fun create(sourceCode: String, customParameters: Map<String, String>, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: List<Palette?>): FractProperties {
            val program = FractlangProgram(sourceCode, customParameters)

            return create(program, customScale, customShaderProperties, customPalettes)
        }

        fun create(program: FractlangProgram, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: List<Palette?>): FractProperties {
            return FractProperties(program.sourceCode,
                customScale,
                customShaderProperties,
                customPalettes,
                program.defaultScale,
                fallBackShaderProperties,
                program.defaultPalettes,
                program.activeParameters,
                program.vmCode
            )
        }

        private val fallBackShaderProperties = ShaderProperties(false)
    }
}