package at.searles.fractbitmapmodel

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram
import at.searles.paletteeditor.PaletteAdapter
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialization
 */
object FractPropertiesAdapter {
    fun toBundle(props: FractProperties): Bundle {
        val bundle = Bundle()

        if(!props.isDefaultScale) {
            bundle.putDoubleArray(scaleKey, scaleToArray(props.scale))
        }

        bundle.putBundle(parametersKey, parametersToBundle(props.customParameters))

        bundle.putSparseParcelableArray(palettesKey, palettesToArray(props.palettes))

        if(!props.isDefaultShaderProperties) {
            bundle.putBundle(shaderPropertiesKey, props.shaderProperties.toBundle())
        }

        bundle.putString(sourceCodeKey, props.sourceCode)

        return bundle
    }

    fun fromBundle(bundle: Bundle): FractProperties {
        val scale = bundle.getDoubleArray(scaleKey)?.let {
            scaleFromArray(it)
        }

        val parameters = parametersFromBundle(bundle.getBundle(parametersKey)!!)

        val palettes = palettesFromArray(bundle.getSparseParcelableArray<Bundle>(palettesKey)!!)

        val shaderProperties = bundle.getBundle(shaderPropertiesKey)?.let {
            ShaderProperties.fromBundle(it)
        }

        val sourceCode = bundle.getString(sourceCodeKey)!!

        val program = FractlangProgram(sourceCode, parameters)

        return FractProperties(program, scale, shaderProperties, palettes)
    }

    fun toJson(props: FractProperties): JSONObject {
        val obj = JSONObject()

        if(!props.isDefaultScale) {
            obj.put(scaleKey, scaleToJson(props.scale))
        }

        obj.put(parametersKey, JSONObject(props.customParameters))

        obj.put(palettesKey, palettesToJson(props))

        if(!props.isDefaultShaderProperties) {
            obj.put(shaderPropertiesKey, props.shaderProperties.toJson())
        }

        obj.put(sourceCodeKey, props.sourceCode)

        return obj
    }

    private fun palettesToJson(props: FractProperties): JSONArray {
        val palettesArray = JSONArray()

        repeat(props.paletteCount) {
            if(props.isDefaultPalette(it)) {
                palettesArray.put(null)
            } else {
                palettesArray.put(PaletteAdapter.toJson(props.getPalette(it)))
            }
        }

        return palettesArray
    }

    private fun palettesToArray(palettes: List<Palette?>): SparseArray<out Parcelable> {
        val array = SparseArray<Parcelable>()

        palettes.forEachIndexed { index, palette ->
            if(palette != null) {
                array.put(index, PaletteAdapter.toBundle(palette))
            }
        }

        return array
    }

    private fun palettesFromArray(array: SparseArray<Bundle>): List<Palette?> {
        val list = ArrayList<Palette?>()

        repeat(array.size()) {
            val index = array.keyAt(it)
            val palette = PaletteAdapter.toPalette(array.valueAt(it))

            while(list.size <= index) {
                list.add(null)
            }

            list[index] = palette
        }

        return list
    }

    private fun scaleToJson(scale: Scale): JSONArray {
        val scaleArray = JSONArray()

        // order consistent with constructor in Scale
        scaleArray.put(scale.xx).put(scale.xy).put(scale.yx).put(scale.yy).put(scale.cx).put(scale.cy)

        return scaleArray
    }

    fun fromJson(obj: JSONObject): FractProperties {
        val customScale: Scale? = if(obj.has(scaleKey)) {
            scaleFromJson(obj.getJSONArray(scaleKey))
        } else {
            null
        }

        val customParameters = parametersFromJson(obj.getJSONObject(parametersKey))

        val customPalettes = palettesFromJson(obj.getJSONArray(palettesKey))

        val customShaderProperties: ShaderProperties? = if(obj.has(shaderPropertiesKey)) {
            ShaderProperties.fromJson(obj.getJSONObject(shaderPropertiesKey))
        } else {
            null
        }

        val sourceCode = obj.getString(sourceCodeKey)

        return FractProperties.create(sourceCode, customParameters, customScale, customShaderProperties, customPalettes)
    }

    fun palettesFromJson(palettesArray: JSONArray): List<Palette?> {
        val palettes = ArrayList<Palette?>()

        for(i in 0 until palettesArray.length()) {
            palettes.add(
                if(palettesArray.isNull(i)) {
                    null
                } else {
                    PaletteAdapter.toPalette(palettesArray.getJSONObject(i))
                }
            )
        }

        return palettes
    }

    fun scaleFromJson(scaleArray: JSONArray): Scale {
        return Scale(
            scaleArray.getDouble(0),
            scaleArray.getDouble(1),
            scaleArray.getDouble(2),
            scaleArray.getDouble(3),
            scaleArray.getDouble(4),
            scaleArray.getDouble(5))
    }

    fun scaleFromArray(scaleArray: DoubleArray): Scale {
        return Scale(
            scaleArray[0],
            scaleArray[1],
            scaleArray[2],
            scaleArray[3],
            scaleArray[4],
            scaleArray[5])
    }

    fun scaleToArray(scale: Scale): DoubleArray {
        return doubleArrayOf(
            scale.xx, scale.xy, scale.yx, scale.yy, scale.cx, scale.cy
        )
    }

    fun parametersFromJson(obj: JSONObject): Map<String, String> {
        // parametersToJson is "JSONObject(parameters)"
        val parameters = HashMap<String, String>()
        obj.keys().forEach { key -> parameters[key] = obj.getString(key) }

        return parameters
    }

    fun parametersToBundle(parameters: Map<String, String>): Bundle {
        val bundle = Bundle()

        parameters.forEach { (key, value) ->
            bundle.putString(key, value)
        }

        return bundle
    }

    fun parametersFromBundle(bundle: Bundle): Map<String, String> {
        return bundle.keySet().map { it to bundle.getString(it)!! }.toMap()
    }

    const val sourceCodeKey = "sourceCode"
    const val parametersKey = "parameters"
    const val scaleKey = "scale"

    const val palettesKey = "palettes"
    const val shaderPropertiesKey = "shaderProperties"

}