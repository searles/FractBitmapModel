package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript

class BitmapAllocation(rs: RenderScript, val width: Int, val height: Int) {

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

    val rsBitmap = Allocation.createFromBitmap(rs, bitmap)

    fun syncBitmap() {
        rsBitmap.copyTo(bitmap)
    }
}