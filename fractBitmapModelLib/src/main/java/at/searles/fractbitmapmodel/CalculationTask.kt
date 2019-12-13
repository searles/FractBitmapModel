package at.searles.fractbitmapmodel

import android.os.AsyncTask
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.searles.fractbitmapmodel.ScriptC_calc
import kotlin.math.abs

class CalculationTask(private val rs: RenderScript, val width: Int, val height: Int,
                      private val bitmapData: Allocation, private val calcScript: ScriptC_calc
): AsyncTask<Unit?, Int, Unit?>() {

    lateinit var listener: Listener

    override fun onPreExecute() {
        listener.started()
    }

    override fun doInBackground(vararg param: Unit?) {
        val part = Allocation.createSized(rs, Element.U8(rs),
            parallelCalculationsCount
        )

        val ceilLog2Width = ceilLog2(width + 1)
        val ceilLog2Height = ceilLog2(height + 1)

        calcScript._bitmapData = bitmapData

        calcScript._ceilLog2Width = ceilLog2Width
        calcScript._ceilLog2Height = ceilLog2Height

        val count = 1 shl (ceilLog2Width + ceilLog2Height)

        var index = 0

        while(index < count) {
            calcScript._pixelIndex0 = index.toLong()
            calcScript.forEach_calculate_part(part)
            rs.finish()

            index += parallelCalculationsCount

            val pixelGap = if(index < count) {
                getPixelGapAfterIndex(index, ceilLog2Width, ceilLog2Height)
            } else {
                1
            }

            publishProgress(index, count, pixelGap)

            if(isCancelled) {
                return
            }
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val index = values[0]!!
        val count = values[1]!!
        val pixelGap = values[2]!!

        listener.progress(index.toFloat() / count.toFloat(), pixelGap)
    }

    override fun onCancelled(result: Unit?) {
        onPostExecute(result)
    }

    override fun onPostExecute(result: Unit?) {
        listener.finished()
    }

    private fun getPixelGapAfterIndex(index: Int, ceilLog2Width: Int, ceilLog2Height: Int): Int {
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

    interface Listener {
        fun started()
        /**
         * This method is called at least once, even if the task is cancelled.
         */
        fun progress(progress: Float, pixelGap: Int)
        fun finished()
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}