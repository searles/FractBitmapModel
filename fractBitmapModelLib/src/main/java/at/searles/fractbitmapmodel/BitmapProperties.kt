package at.searles.fractbitmapmodel

import android.util.SparseArray
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb
import org.json.JSONArray
import org.json.JSONObject

class BitmapProperties (
    palettes: List<Palette>,
    val shaderProperties: ShaderProperties
) {
    val palettes = if(palettes.isEmpty()) defaultPalettes else palettes

    fun createJson(): JSONObject {
        val palettesArray = JSONArray()
        palettes.forEach { palettesArray.put(it.createJson()) }

        val obj = JSONObject()
        obj.put(palettesKey, palettesArray)

        obj.put(shaderPropertiesKey, shaderProperties.createJson())

        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): BitmapProperties {
            val palettesArray = obj.getJSONArray(palettesKey)
            val palettes = ArrayList<Palette>()

            for(i in 0 until palettesArray.length()) {
                palettes.add(Palette.fromJson(palettesArray.getJSONObject(i)))
            }

            val shaderPropertiesObj = obj.getJSONObject(shaderPropertiesKey)
            val shaderProperties = ShaderProperties.fromJson(shaderPropertiesObj)

            return BitmapProperties(palettes, shaderProperties)
        }

        const val palettesKey = "palettes"
        const val shaderPropertiesKey = "shaderProperties"

        val defaultPalettes = listOf(
            Palette(1, 1, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(0f, 0f, 0f).toLab())
                    })
                })
        )
    }
}