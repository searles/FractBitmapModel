package at.searles.fractbitmapprovider.fractalbitmapmodel

import android.os.AsyncTask
import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.ScriptC_calc

class CalculationTask(private val calcScript: ScriptC_calc): AsyncTask<BitmapAllocation, Void, Unit>() {
    lateinit var listener: Listener

    var isRunning: Boolean = true
        private set

    override fun onPreExecute() {
        notifyStarted()
    }

    override fun doInBackground(vararg params: BitmapAllocation) {
        val bitmapMemento = params[0]
        calcScript.forEach_calculate(bitmapMemento.bitmapData, bitmapMemento.bitmapData)
        bitmapMemento.syncBitmap()
        listener.updated()
    }

    override fun onPostExecute(result: Unit) {
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