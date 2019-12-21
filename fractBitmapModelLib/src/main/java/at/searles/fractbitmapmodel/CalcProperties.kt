package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.util.SparseArray
import androidx.core.graphics.values
import androidx.core.util.set
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.PaletteData
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb
import org.json.JSONArray
import org.json.JSONObject

/**
 * Collection of all properties that are needed for the Calc-Script.
 */
class CalcProperties (
    scale: Scale?,
    private val fractlangProgram: FractlangProgram
) {
    val scale: Scale = scale ?: defaultScale // kotlin, I am impressed.
    val sourceCode = fractlangProgram.sourceCode
    val parameters = fractlangProgram.activeParameters
    val vmCode = fractlangProgram.vmCode

    fun createWithRelativeScale(relativeMatrix: Matrix): CalcProperties {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return CalcProperties(
            newScale,
            fractlangProgram
        )
    }

    fun createWithNewScale(newScale: Scale): CalcProperties {
        return CalcProperties(
            newScale,
            fractlangProgram
        )
    }

    fun createWithNewSourceCode(newFractlangProgram: FractlangProgram): CalcProperties {
        return CalcProperties(
            scale,
            newFractlangProgram
        )
    }

    fun createWithResetParameter(parameterKey: String): CalcProperties {
        val newParameters = parameters.toMutableMap().apply {
            remove(parameterKey)
        }

        val newFractlangProgram = FractlangProgram(sourceCode, newParameters)

        return CalcProperties(
            scale,
            newFractlangProgram
        )
    }

    fun createWithNewParameter(parameterKey: String, value: String): CalcProperties {
        val newParameters = parameters.toMutableMap().apply {
            this[parameterKey] = value
        }

        val newFractlangProgram = FractlangProgram(sourceCode, newParameters)

        return CalcProperties(
            scale,
            newFractlangProgram
        )
    }

    fun createJson(obj: JSONObject): JSONObject {
        val parametersObj = JSONObject()
        parameters.forEach { (key, value) -> parametersObj.put(key, value) }

        obj.put(sourceCodeKey, sourceCode)
        obj.put(parametersKey, parametersObj)

        val scaleArray = JSONArray()

        // order consistent with constructor in Scale
        scaleArray.put(scale.xx).put(scale.xy).put(scale.yx).put(scale.yy).put(scale.cx).put(scale.cy)
        obj.put(scaleKey, scaleArray)

        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): CalcProperties {
            val parametersObj = obj.getJSONObject(parametersKey)

            val parameters = HashMap<String, String>()
            parametersObj.keys().forEach { key -> parameters[key] = parametersObj.getString(key) }

            val scaleArray = obj.getJSONArray(scaleKey)
            val scale = Scale(
                scaleArray.getDouble(0),
                scaleArray.getDouble(1),
                scaleArray.getDouble(2),
                scaleArray.getDouble(3),
                scaleArray.getDouble(4),
                scaleArray.getDouble(5))

            val sourceCode = obj.getString(sourceCodeKey)

            val fractlangProgram = FractlangProgram(sourceCode, parameters)

            return CalcProperties(scale, fractlangProgram)
        }

        fun getScale(doubleArray: DoubleArray?): Scale? = doubleArray ?.let { toScale(it) }
        fun getPalettes(paletteDataList: List<PaletteData>): List<Palette> = paletteDataList.map { toPalette(it) }

        private fun toPalette(paletteData: PaletteData): Palette {
            val colorPoints = SparseArray<SparseArray<Lab>>()

            (paletteData.points).forEach {
                val x = it[0]
                val y = it[1]
                val color = it[2]

                val row = colorPoints.get(y, SparseArray())
                row[x] = Rgb.of(color).toLab()
                colorPoints[y] = row
            }

            return Palette(paletteData.width, paletteData.height, 0f, 0f, colorPoints)
        }

        private fun toScale(array: DoubleArray): Scale {
            return Scale(array[0], array[1], array[2], array[3], array[4], array[5])
        }

        const val sourceCodeKey = "sourceCode"
        const val parametersKey = "parameters"
        const val scaleKey = "scale"

        val defaultScale = Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0)
    }
}