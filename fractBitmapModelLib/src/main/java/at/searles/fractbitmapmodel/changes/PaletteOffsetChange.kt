package at.searles.fractbitmapmodel.changes

import at.searles.commons.color.Palette
import at.searles.fractbitmapmodel.FractProperties

class PaletteOffsetChange(private val index: Int, private val offsetX: Float, private val offsetY: Float, override val useFastRoot: Boolean = false): BitmapPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        val palettes = properties.palettes.toMutableList()
        palettes[index] = Palette(palettes[index].width, palettes[index].height, offsetX, offsetY, palettes[index].colorPoints)
        return properties.createWithNewBitmapProperties(palettes, null)
    }
}