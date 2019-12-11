package at.searles.fractbitmapprovider.palette

import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import at.searles.commons.math.InterpolationMatrix
import at.searles.fractbitmapprovider.ScriptC_bitmap
import at.searles.fractbitmapprovider.ScriptField_palette
import at.searles.fractbitmapprovider.ScriptField_paletteSegment
import at.searles.paletteeditor.Palette
import java.util.*

class PaletteUpdater(private val rs: RenderScript, private val script: ScriptC_bitmap) {

    fun updateOffsets(index: Int, offsetX: Float, offsetY: Float) {
        script._palettes.set_offsetX(index, offsetX, false)
        script._palettes.set_offsetY(index, offsetY, false)

        script._palettes.copyAll()
    }

    fun updatePalettes(palettes: List<Palette>) {
        val splineSegments = ArrayList<SplineSegment>()

        val palettesWithSegmentIndex = palettes.map {palette ->
            val segmentStartIndex = splineSegments.size
            val segments = createSplineSegments(palette)

            segments.forEach { splineSegments.addAll(it) }

            Pair(palette, segmentStartIndex)
        }

        setPalettesInScript(palettesWithSegmentIndex)
        setSegmentsInScript(splineSegments)
    }

    private fun createSplineSegments(palette: Palette): Array<Array<SplineSegment>> {
        val height = palette.height
        val width = palette.width
        val colors = palette.colorTable

        // Create a matrix of all lab-a components

        val L = Array(height) { DoubleArray(width) }
        val a = Array(height) { DoubleArray(width) }
        val b = Array(height) { DoubleArray(width) }
        val alpha = Array(height) { DoubleArray(width) }

        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                with(colors[y][x]) {
                    L[y][x] = this.l.toDouble()
                    a[y][x] = this.a.toDouble()
                    b[y][x] = this.b.toDouble()
                    alpha[y][x] = this.alpha.toDouble()
                }
            }
        }

        val LSpline = InterpolationMatrix.create(L)
        val aSpline = InterpolationMatrix.create(a)
        val bSpline = InterpolationMatrix.create(b)
        val alphaSpline = InterpolationMatrix.create(alpha)

        return Array(height) {y ->
            Array(width) { x ->
                SplineSegment(
                    LSpline[y][x],
                    aSpline[y][x],
                    bSpline[y][x],
                    alphaSpline[y][x]
                )
            }
        }
    }

    private fun setSegmentsInScript(splineSegments: ArrayList<SplineSegment>) {
        val rsSegments = ScriptField_paletteSegment(rs, splineSegments.size)
        script.bind_segments(rsSegments)

        splineSegments.forEachIndexed { index, splineSegment ->
            rsSegments.set_L(index, Matrix4f(splineSegment.L.flts()), false)
            rsSegments.set_a(index, Matrix4f(splineSegment.a.flts()), false)
            rsSegments.set_b(index, Matrix4f(splineSegment.b.flts()), false)
            rsSegments.set_alpha(index, Matrix4f(splineSegment.alpha.flts()), false)
        }

        script._segments.copyAll()
    }

    private fun setPalettesInScript(palettesWithSegmentIndex: List<Pair<Palette, Int>>) {
        val rsPalettes = ScriptField_palette(rs, palettesWithSegmentIndex.size)
        script.bind_palettes(rsPalettes)

        palettesWithSegmentIndex.forEachIndexed { index, pair ->
            val palette = pair.first
            val segmentIndex = pair.second

            rsPalettes.set_w(index, palette.width.toLong(), false)
            rsPalettes.set_h(index, palette.height.toLong(), false)

            rsPalettes.set_offsetX(index, palette.offsetX, false)
            rsPalettes.set_offsetY(index, palette.offsetY, false)

            rsPalettes.set_segmentIndex(index, segmentIndex.toLong(), false)
        }

        script._palettes.copyAll()
    }

}