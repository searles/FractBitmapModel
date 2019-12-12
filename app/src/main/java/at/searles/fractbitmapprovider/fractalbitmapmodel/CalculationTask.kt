package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.os.AsyncTask
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.ScriptC_bitmap
import at.searles.fractbitmapprovider.ScriptC_calc
import at.searles.fractbitmapprovider.ScriptC_interpolate_gaps
import kotlin.math.abs
import kotlin.math.max

class CalculationTask(private val rs: RenderScript,
                      private val calcScript: ScriptC_calc,
                      private val bitmapScript: ScriptC_bitmap,
                      private val interpolateGapsScript: ScriptC_interpolate_gaps,
                      private val bitmapAllocation: BitmapAllocation,
                      private val listener: Listener): AsyncTask<Unit?, Unit?, Unit?>() {
    var isRunning: Boolean = true
        private set

    override fun onPreExecute() {
        notifyStarted()
    }

    override fun doInBackground(vararg param: Unit?) {
        val part = Allocation.createSized(rs, Element.U8(rs), parallelCalculationsCount)

        calcScript._bitmapData = bitmapAllocation.bitmapData

        val ceilLog2Width = ceilLog2(bitmapAllocation.width + 1)
        val ceilLog2Height = ceilLog2(bitmapAllocation.height + 1)

        calcScript._ceilLog2Width = ceilLog2Width
        calcScript._ceilLog2Height = ceilLog2Height

        val count = 1 shl (ceilLog2Width + ceilLog2Height)

        //var pixelDistance = 1 shl max(ceilLog2Width, ceilLog2Width)
        //bitmapAllocation.pixelDistance = pixelDistance

        var index = 0

        while(!isCancelled) {
            calcScript._pixelIndex0 = index.toLong()
            calcScript.forEach_calculate_part(part)
            rs.finish()

            index += parallelCalculationsCount

            notifyProgress(index.toFloat() / count.toFloat())

            if(index >= count) {
                break
            }

            val nextPixelDistance = getPixelDistanceAfterIndex(index, ceilLog2Width, ceilLog2Height)

            bitmapAllocation.pixelDistance = nextPixelDistance
            bitmapAllocation.syncBitmap()

            notifyUpdate()
        }

        bitmapAllocation.pixelDistance = 1
        bitmapAllocation.syncBitmap()

        notifyUpdate()
    }

    override fun onCancelled(result: Unit?) {
        onPostExecute(result)
    }

    override fun onPostExecute(result: Unit?) {
        isRunning = false
        notifyFinished()
    }

    private fun getPixelDistanceAfterIndex(index: Int, ceilLog2Width: Int, ceilLog2Height: Int): Int {
        val pair = getPixelCoordinates(index + 1, ceilLog2Width, ceilLog2Height)

        var x = pair.first
        var y = pair.second

        if(x == 0 && y == 0) return 1

        var stepSize = 2

        while(x or y and 0x1 == 0) {
            x = x shr 1
            y = y shr 1
            stepSize = stepSize shl 1
        }

        return stepSize
    }

    private fun getPixelCoordinates(index: Int, n: Int, m: Int): Pair<Int, Int> {
        var x = 0
        var y = 0

        for(a in 0 until n - Integer.min(n, m)) {
            x = (x shl 1) or (index shr a and 1)
        }

        for(a in 0 until m - Integer.min(n, m)) {
            y = (y shl 1) or (index shr a and 1)
        }

        for(a in abs(n - m) until n + m step 2) {
            y = (y shl 1) or (index shr (a + 1) and 1)
            x = (x shl 1) or (index shr a and 1)
        }

        return Pair(x, y)
    }

    private fun ceilLog2(k: Int): Int {
        for (n in 0..31) {
            if (1 shl n >= k) return n
        }

        return -1
    }

    private fun notifyStarted() {
        listener.started()
    }

    private fun notifyUpdate() {
        listener.updated()
    }

    private fun notifyFinished() {
        listener.finished()
    }

    private fun notifyProgress(progress: Float) {
        listener.progress(progress)
    }

    interface Listener {
        fun started()
        fun updated()
        fun finished()
        fun progress(progress: Float)
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}