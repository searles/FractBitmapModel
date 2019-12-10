package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.graphics.Matrix
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.Float3
import android.renderscript.RenderScript
import android.util.SparseArray
import at.searles.fractbitmapprovider.palette.PaletteWrapper
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb

class RenderScriptBitmapModel(val rs: RenderScript): CoordinatesBitmapModel() {

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)

    override val bitmap: Bitmap
        get() = bitmapMemento.bitmap

    /**
     * This matrix is used to transform the shown image.
     */
    override val normMatrix: Matrix = Matrix()

    val paletteWrapper = PaletteWrapper(rs, bitmapScript).apply {
        palettes = listOf(
            Palette(4, 4, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(1f, 0f, 0f).toLab())
                        row.put(2, Rgb(0f, 0f, 0f).toLab())
                    })
                    table.put(1, SparseArray<Lab>().also { row ->
                        row.put(1, Rgb(1f, 1f, 0f).toLab())
                        row.put(3, Rgb(0f, 0.5f, 0f).toLab())
                    })
                    table.put(2, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(0f, 0f, 1f).toLab())
                        row.put(2, Rgb(1f, 1f, 1f).toLab())
                    })
                    table.put(3, SparseArray<Lab>().also { row ->
                        row.put(1, Rgb(1f, 0.5f, 0.5f).toLab())
                        row.put(3, Rgb(0.5f, 0.5f, 1f).toLab())
                    })
                }),
            Palette(1, 1, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(1f, 0f, 0f).toLab())
                    })
                })
        )

        this.updatePalettes()
    }

    lateinit var bitmapMemento: BitmapMemento
        private set

    override fun notifyScaleRequested() {
        popNormMatrix()
        updateScaleInScripts()
        calc()
    }

    /**
     * Converts the current scale to image coordinates and
     * sets it in renderscript. calc must be called afterwards.
     */
    private fun updateScaleInScripts() {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val factor = 1.0 / if (centerX < centerY) centerX else centerY

        val scale = ScriptField_Scale.Item().apply {
            a = scale.xx * factor
            b = scale.yx * factor
            c = scale.xy * factor
            d = scale.yy * factor

            e = scale.cx - (a * centerX + b * centerY)
            f = scale.cy - (c * centerX + d * centerY)
        }

        calcScript._scale = scale
        bitmapScript._scale = scale
    }

    /**
     * Update lightVector. syncBitmap must be called afterwards.
     */
    private val lightVector: Float3 = Float3(1f, 0f, 0f)

    fun setLightVector(x: Float, y: Float, z: Float) {
        lightVector.x = x
        lightVector.y = y
        lightVector.z = z

        bitmapScript._lightVector = lightVector
    }

    /**
     * Performs the calculation.
     */
    fun calc() {
        calcScript.forEach_calculate(bitmapMemento.bitmapData, bitmapMemento.bitmapData)
        bitmapMemento.syncBitmap()
    }

    fun createBitmapMemento(width: Int, height: Int): BitmapMemento {
        return BitmapMemento(width, height)
    }

    fun setBitmapMemento(value: BitmapMemento) {
        bitmapMemento = value
        this.bitmapScript.bind_bitmapData(value.bitmapData)

        this.calcScript._width = value.width
        this.calcScript._height = value.height

        this.bitmapScript._width = value.width
        this.bitmapScript._height = value.height

        updateScaleInScripts()
    }

    inner class BitmapMemento(val width: Int, val height: Int) {
        val bitmapData: Allocation = Allocation.createSized(rs, Element.F32_3(rs), (width + 1) * (height + 1))
        val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        private val rsBitmap: Allocation

        init {
            rsBitmap = Allocation.createFromBitmap(rs, bitmap)
        }

        /**
         * Renders bitmapData into the bitmap using the current parameters.
         */
        fun syncBitmap() {
            bitmapScript.forEach_root(rsBitmap)
            rsBitmap.copyTo(bitmap)
        }
    }
}