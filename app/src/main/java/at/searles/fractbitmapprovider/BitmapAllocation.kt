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

    fun fastSyncBitmap(stepSize: Int, bitmapTiledScript: ScriptC_bitmap, interpolateGapsScript: ScriptC_interpolate_gaps) {
        interpolateGapsScript._bitmap = rsBitmap
        bitmapTiledScript._stepSize = stepSize.toLong()
        interpolateGapsScript._stepSize = stepSize.toLong()

        bitmapTiledScript.forEach_fastRoot(rsBitmap)
        interpolateGapsScript.forEach_root(rsBitmap)

        rsBitmap.copyTo(bitmap)
    }
}