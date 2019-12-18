package at.searles.fractbitmapmodel

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript

/**
 * Class representing a bitmap and associated data structures.
 */
class BitmapAllocation(rs: RenderScript, val width: Int, val height: Int) {

    val calcData: Allocation =
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

    val rsBitmap: Allocation = Allocation.createFromBitmap(rs, bitmap)

    fun sync() {
        rsBitmap.copyTo(bitmap)
    }
}