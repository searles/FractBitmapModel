package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript

class BitmapAllocation(val rs: RenderScript, val width: Int, val height: Int) {
    val bitmapData: Allocation =
        Allocation.createSized(
            rs,
            Element.F32_3(rs),
            (width + 1) * (height + 1)
        )
    val bitmap: Bitmap =
        Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    private val rsBitmap: Allocation

    init {
        rsBitmap = Allocation.createFromBitmap(rs, bitmap)
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun syncBitmap(bitmapScript: ScriptC_bitmap) {
        bitmapScript.forEach_root(rsBitmap)
        rsBitmap.copyTo(bitmap)
    }
}