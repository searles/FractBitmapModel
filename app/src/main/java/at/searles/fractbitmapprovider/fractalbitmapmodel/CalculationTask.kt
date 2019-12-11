package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.os.AsyncTask
import android.renderscript.RenderScript
import android.util.Log
import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.ScriptC_bitmap
import at.searles.fractbitmapprovider.ScriptC_calc

class CalculationTask(private val rs: RenderScript, private val calcScript: ScriptC_calc, private val bitmapScript: ScriptC_bitmap, private val listener: Listener): AsyncTask<BitmapAllocation, Unit?, Unit?>() {
    var isRunning: Boolean = true
        private set

    override fun onPreExecute() {
        notifyStarted()
    }

    override fun doInBackground(vararg params: BitmapAllocation) {
        val bitmapAllocation = params[0]
        calcScript.forEach_calculate(bitmapAllocation.bitmapData, bitmapAllocation.bitmapData)
        bitmapAllocation.syncBitmap(bitmapScript)
        listener.updated()
    }

    override fun onCancelled(result: Unit?) {
        onPostExecute(result)
    }

    override fun onPostExecute(result: Unit?) {
        isRunning = false
        notifyFinished()
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
}