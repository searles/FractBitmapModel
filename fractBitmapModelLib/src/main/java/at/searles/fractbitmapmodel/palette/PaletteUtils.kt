package at.searles.fractbitmapmodel.palette

import android.util.SparseArray
import androidx.core.util.set
import at.searles.fractlang.PaletteData
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb

object PaletteUtils {
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

}