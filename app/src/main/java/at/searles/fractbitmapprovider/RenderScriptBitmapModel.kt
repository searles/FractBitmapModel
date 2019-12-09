package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.graphics.Matrix
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.Float3
import android.renderscript.RenderScript

class RenderScriptBitmapModel(val rs: RenderScript): CoordinatesBitmapModel() {

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)

    override val bitmap: Bitmap
        get() = bitmapMemento.bitmap

    /**
     * This matrix is used to transform the shown image.
     */
    override val normMatrix: Matrix = Matrix()

    lateinit var bitmapMemento: BitmapMemento
        private set

    fun setBitmapMemento(value: BitmapMemento) {
        bitmapMemento = value
        this.bitmapScript.bind_bitmapData(value.bitmapData)

        this.calcScript._width = value.width
        this.calcScript._height = value.height

        this.bitmapScript._width = value.width
        this.bitmapScript._height = value.height

        updateScaleInScripts()
    }

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

    fun createBitmapMemento(width: Int, height: Int): BitmapMemento {
        return BitmapMemento(width, height)
    }

    private val lightVector: Float3 = Float3(1f, 0f, 0f)

    fun setLightVector(x: Float, y: Float, z: Float) {
        lightVector.x = x
        lightVector.y = y
        lightVector.z = z
    }

    override fun notifyScaleRequested() {
        popNormMatrix()
        updateScaleInScripts()
        calc()
    }

    fun calc() {
        calcScript.forEach_calculate(bitmapMemento.bitmapData, bitmapMemento.bitmapData)
    }

    inner class BitmapMemento(val width: Int, val height: Int) {
        val bitmapData: Allocation
        private val rsBitmap: Allocation
        val bitmap: Bitmap

        init {
            bitmapData = Allocation.createSized(rs, Element.F32_3(rs), (width + 1) * (height + 1))
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            rsBitmap = Allocation.createFromBitmap(rs, bitmap)
        }

        fun syncBitmap() {
            bitmapScript._lightVector = lightVector

            bitmapScript.forEach_root(rsBitmap)
            rsBitmap.copyTo(bitmap)
        }
    }

    // TODO
    // Command pattern
    //
}