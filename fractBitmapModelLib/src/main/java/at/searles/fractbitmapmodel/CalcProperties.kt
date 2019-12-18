package at.searles.fractbitmapmodel

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.fractlang.CompilerInstance

class CalcProperties (
    val scale: Scale,
    val sourceCode: String,
    val parameters: Map<String, String>,
    private val compilerInstance: CompilerInstance = CompilerInstance(sourceCode, parameters).apply { compile() }
) {
    // TODO: Number of palettes and initial palette from compiler instance
    // TODO: Initial scale from compilerInstance.
    // TODO: Initial shaderProperties from compilerInstance

    val vmCode = compilerInstance.vmCode

    fun createWithRelativeScale(relativeMatrix: Matrix): CalcProperties {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return CalcProperties(
            newScale,
            sourceCode,
            parameters,
            compilerInstance
        )
    }

    fun createWithNewScale(newScale: Scale): CalcProperties {
        return CalcProperties(
            newScale,
            sourceCode,
            parameters,
            compilerInstance
        )
    }

    fun createWithNewSourceCode(newSourceCode: String): CalcProperties {
        return CalcProperties(
            scale,
            newSourceCode,
            parameters
        )
    }

    fun createWithNewAsset(newSourceCode: String, newParameters: Map<String, String>): CalcProperties {
        return CalcProperties(
            scale,
            newSourceCode,
            newParameters
        )
    }

    fun createWithResetParameter(parameterKey: String): CalcProperties {
        val newParameters = parameters.toMutableMap().apply {
            remove(parameterKey)
        }
        return CalcProperties(
            scale,
            sourceCode,
            newParameters
        )
    }

    fun createWithNewParameter(parameterKey: String, value: String): CalcProperties {
        val newParameters = parameters.toMutableMap().apply {
            this[parameterKey] = value
        }

        return CalcProperties(
            scale,
            sourceCode,
            newParameters
        )
    }
}