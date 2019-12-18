package at.searles.fractbitmapmodel

import at.searles.fractbitmapmodel.ShaderProperties
import at.searles.paletteeditor.Palette

class BitmapProperties (
    val palettes: List<Palette>,
    val shaderProperties: ShaderProperties
) {
    // TODO: Number of palettes and initial palette from compiler instance
    // TODO: Initial scale from compilerInstance.
    // TODO: Initial shaderProperties from compilerInstance
}