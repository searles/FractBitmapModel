package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.os.AsyncTask
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.ScriptC_bitmap
import at.searles.fractbitmapprovider.ScriptC_calc
import at.searles.fractbitmapprovider.ScriptC_interpolate_gaps
import kotlin.math.max

class CalculationTask(private val rs: RenderScript,
                      private val calcScript: ScriptC_calc,
                      private val bitmapScript: ScriptC_bitmap,
                      private val interpolateGapsScript: ScriptC_interpolate_gaps,
                      private val listener: Listener): AsyncTask<BitmapAllocation, Unit?, Unit?>() {
    var isRunning: Boolean = true
        private set

    override fun onPreExecute() {
        notifyStarted()
    }

    override fun doInBackground(vararg params: BitmapAllocation) {
        val tile = Allocation.createSized(rs, Element.U8(rs), tileSize * tileSize)

        val bitmapAllocation = params[0]

        calcScript._bitmapData = bitmapAllocation.bitmapData

        var stepSize = getInitalStepSize(bitmapAllocation) // this is a power of tileSize.

        calcScript._initialStepSize = stepSize.toLong()
        calcScript._tileSize = tileSize.toLong()

        while(stepSize > 0) {
            calcScript._stepSize = stepSize.toLong()
            val tileDimension = stepSize * tileSize

            // The size is actually bitmapAllocation + 1, and we want to round up.
            val tilesPerRow = (bitmapAllocation.width + tileDimension) / tileDimension
            val tilesPerCol = (bitmapAllocation.height + tileDimension) / tileDimension

            val tilesCount = tilesPerRow * tilesPerCol

            repeat(tilesCount) {
                if(isCancelled) {
                    return
                }

                val tileX0 = (it % tilesPerRow) * tileDimension
                val tileY0 = (it / tilesPerRow) * tileDimension

                calcScript._tileX0 = tileX0.toLong()
                calcScript._tileY0 = tileY0.toLong()

                calcScript.forEach_calculate_tile(tile)
            }

            rs.finish()

            /*if(stepSize == 1) {
                // Do the nice one.
                bitmapAllocation.syncBitmap(bitmapScript)
                notifyUpdate()
                break
            } else {*/
                bitmapAllocation.fastSyncBitmap(stepSize, bitmapScript, interpolateGapsScript)
                notifyUpdate()
            //}

            stepSize /= tileSize
        }
    }

    override fun onCancelled(result: Unit?) {
        onPostExecute(result)
    }

    override fun onPostExecute(result: Unit?) {
        isRunning = false
        notifyFinished()
    }

    private fun getInitalStepSize(bitmapAllocation: BitmapAllocation): Int {
        var stepSize = 1

        while(stepSize * tileSize * tileSize < max(bitmapAllocation.width, bitmapAllocation.height)) {
            stepSize *= tileSize
        }

        return stepSize
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
        const val tileSize = 128
    }
}