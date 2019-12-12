package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import kotlin.math.max

class BitmapAllocation(val width: Int, val height: Int,
                       scripts: ScriptsInstance) {

    private val bitmapScript = scripts.bitmapScript
    private val interpolateGapsScript = scripts.interpolateGapsScript

    val bitmapData: Allocation =
        Allocation.createSized(
            scripts.rs,
            Element.F32_3(scripts.rs),
            (width + 1) * (height + 1)
        )
    val bitmap: Bitmap =
        Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    private val rsBitmap: Allocation

    var pixelDistance: Int = 1

    init {
        rsBitmap = Allocation.createFromBitmap(scripts.rs, bitmap)
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     */
    fun syncBitmap() {
        if(pixelDistance > 1) {
            fastSyncBitmap(pixelDistance)
            return
        }

        bitmapScript.forEach_root(rsBitmap)
        rsBitmap.copyTo(bitmap)
    }

    fun fastSyncBitmap(pixelDistance: Int) {
        val dPix = max(pixelDistance, this.pixelDistance)

        interpolateGapsScript._bitmap = rsBitmap
        bitmapScript._stepSize = dPix.toLong()
        interpolateGapsScript._stepSize = dPix.toLong()

        bitmapScript.forEach_fastRoot(rsBitmap)
        interpolateGapsScript.forEach_root(rsBitmap)

        rsBitmap.copyTo(bitmap)
    }
}