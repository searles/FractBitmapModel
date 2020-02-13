package at.searles.fractbitmapmodel

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
            val paletteOpt = palettesArray.get(i)

            palettes.add(
                if(paletteOpt != null) {
                    PaletteAdapter.toPalette(paletteOpt as JSONObject)
                } else {
                    null
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

    fun parametersFromJson(obj: JSONObject): Map<String, String> {
        val parameters = HashMap<String, String>()
        obj.keys().forEach { key -> parameters[key] = obj.getString(key) }

        return parameters
    }

    const val sourceCodeKey = "sourceCode"
    const val parametersKey = "parameters"
    const val scaleKey = "scale"

    const val palettesKey = "palettes"
    const val shaderPropertiesKey = "shaderProperties"

}