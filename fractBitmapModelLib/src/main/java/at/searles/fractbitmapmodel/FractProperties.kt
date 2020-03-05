package at.searles.fractbitmapmodel

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram

class FractProperties(
    val program: FractlangProgram,
    val customScale: Scale?,
    val customShaderProperties: ShaderProperties?,
    val customPalettes: List<Palette?>) {
    val sourceCode: String = program.sourceCode

    val scale = customScale ?: program.defaultScale
    val isDefaultScale = customScale == null

    val shaderProperties = customShaderProperties ?: fallBackShaderProperties
    val isDefaultShaderProperties = customShaderProperties == null

    val paletteCount: Int = program.defaultPalettes.size

    val palettes: List<Palette> by lazy {
        (0 until paletteCount).map { getPalette(it) }
    }

    val vmCode
            get() = program.vmCode

    fun getPalette(index: Int): Palette {
        require(index in 0 until paletteCount)
        return customPalettes.getOrNull(index) ?: program.defaultPalettes[index].defaultPalette
    }

    fun isDefaultPalette(index: Int): Boolean {
        require(index in 0 until paletteCount)
        return customPalettes.getOrNull(index) == null
    }

    fun getPaletteDescription(index: Int): String {
        require(index in 0 until paletteCount)
        return program.defaultPalettes[index].description
    }

    val parameterIds: Iterable<String> = program.activeParameters.keys

    fun getParameterDescription(id: String): String {
        return program.activeParameters.getValue(id).description
    }

    /**
     * Since FractProgram is compiled using custom parameters,
     * this will be identical to the ones in customParameters.
     */
    fun getParameter(id: String): String {
        return program.activeParameters.getValue(id).expr
    }

    fun isDefaultParameter(id: String): Boolean {
        return program.activeParameters.getValue(id).isDefault
    }

    val customParameters = program.customParameters

    fun createWithNewBitmapProperties(customPalettes: List<Palette?>?, customShaderProperties: ShaderProperties?): FractProperties {
        return FractProperties(program,
            customScale,
            customShaderProperties ?: this.customShaderProperties,
            customPalettes ?: this.customPalettes
        )
    }

    fun createWithRelativeScale(relativeMatrix: Matrix): FractProperties {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return createWithNewScale(newScale)
    }

    fun createWithNewScale(newScale: Scale): FractProperties {
        return FractProperties(program, newScale, customShaderProperties, customPalettes)
    }

    fun createWithNewProperties(newFractlangProgram: FractlangProgram): FractProperties {
        return create(newFractlangProgram, customScale, customShaderProperties, customPalettes)
    }

    companion object {
        fun create(sourceCode: String, customParameters: Map<String, String>, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: List<Palette?>): FractProperties {
            val program = FractlangProgram(sourceCode, customParameters)

            return create(program, customScale, customShaderProperties, customPalettes)
        }

        fun create(program: FractlangProgram, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: List<Palette?>): FractProperties {
            return FractProperties(program,
                customScale,
                customShaderProperties,
                customPalettes
            )
        }

        private val fallBackShaderProperties = ShaderProperties(true)
    }
}