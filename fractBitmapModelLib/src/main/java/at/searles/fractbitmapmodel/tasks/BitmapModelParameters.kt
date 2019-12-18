package at.searles.fractbitmapmodel.tasks

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.Shader3DProperties
import at.searles.fractlang.CompilerInstance
import at.searles.paletteeditor.Palette

class BitmapModelParameters (
    val scale: Scale,
    val palettes: List<Palette>,
    val shader3DProperties: Shader3DProperties,
    val sourceCode: String,
    val parameters: Map<String, String>,
    private val compilerInstance: CompilerInstance = CompilerInstance(sourceCode, parameters).apply { compile() }
) {
    // TODO: Number of palettes and initial palette from compiler instance
    // TODO: Initial scale from compilerInstance.
    // TODO: Initial shaderProperties from compilerInstance

    val vmCode = compilerInstance.vmCode

    fun createWithRelativeScale(relativeMatrix: Matrix): BitmapModelParameters {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return BitmapModelParameters(newScale, palettes, shader3DProperties, sourceCode, parameters, compilerInstance)
    }

    fun createWithNewScale(newScale: Scale): BitmapModelParameters {
        return BitmapModelParameters(newScale, palettes, shader3DProperties, sourceCode, parameters, compilerInstance)
    }

    fun createWithNewSourceCode(newSourceCode: String): BitmapModelParameters {
        return BitmapModelParameters(scale, palettes, shader3DProperties, newSourceCode, parameters)
    }

    fun createWithNewAsset(newSourceCode: String, newParameters: Map<String, String>): BitmapModelParameters {
        return BitmapModelParameters(scale, palettes, shader3DProperties, newSourceCode, newParameters)
    }

    fun createWithResetParameter(parameterKey: String): BitmapModelParameters {
        val newParameters = parameters.toMutableMap().apply {
            remove(parameterKey)
        }
        return BitmapModelParameters(scale, palettes, shader3DProperties, sourceCode, newParameters)
    }

    fun createWithNewParameter(parameterKey: String, value: String): BitmapModelParameters {
        val newParameters = parameters.toMutableMap().apply {
            this[parameterKey] = value
        }

        return BitmapModelParameters(scale, palettes, shader3DProperties, sourceCode, newParameters)
    }
}