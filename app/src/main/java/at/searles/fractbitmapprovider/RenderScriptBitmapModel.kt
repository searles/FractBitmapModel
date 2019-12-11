package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapprovider.fractalbitmapmodel.PostCalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.RelativeScaleTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractbitmapprovider.palette.PaletteUpdater

class RenderScriptBitmapModel(val rs: RenderScript, initialFractal: Fractal, initialBitmapAllocation: BitmapAllocation): LongRunningBitmapModel() {

    var listener: CalculationTask.Listener? = null

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateBitmapInScripts()
            updateScaleInScripts()
        }

    override val bitmap: Bitmap
        get() = bitmapAllocation.bitmap

    var fractal = initialFractal
        set(value) {
            field = value
            updateScaleInScripts()
            updatePalettesInScripts()
        }

    var lightVector: Float3 = Float3(1f, 0f, 0f)
        set(value) {
            field = value
            bitmapScript._lightVector = lightVector
        }

    init {
        updateBitmapInScripts()
        updateScaleInScripts()
        updatePalettesInScripts()
        bitmapScript._lightVector = lightVector
    }

    private var calculationTask: CalculationTask? = null
    private val changes = ArrayList<PostCalculationTask>()

    override fun notifyScaleRequested() {
        registerPostCalcTask(RelativeScaleTask())
    }

    fun setPaletteOffset(index: Int, offsetX: Float, offsetY: Float) {
        PaletteUpdater(rs, bitmapScript).updateOffsets(index, offsetX, offsetY)
    }

    fun syncBitmap() {
        bitmapAllocation.syncBitmap(bitmapScript)
    }

    fun registerPostCalcTask(task: PostCalculationTask) {
        if(calculationTask == null) {
            require(changes.isEmpty())
            task.execute(this)

            if(task.isParameterChange) {
                startTask()
            }
            return
        }

        changes.add(task)
        calculationTask!!.cancel(true)
    }

    private val taskListener = CalcTaskListener()

    init {
        startTask()
    }

    private fun startTask() {
        require(changes.isEmpty())
        require(calculationTask == null)

        // after an update is generated, all scales to this point have been committed.
        notifyStarted()

        calculationTask = CalculationTask(rs, calcScript, bitmapScript, taskListener)

        calculationTask!!.execute(bitmapAllocation)
    }

    private fun updatePalettesInScripts() {
        PaletteUpdater(rs, bitmapScript).apply {
            this.updatePalettes(fractal.palettes)
        }
    }

    private fun updateScaleInScripts() {
        ScaleUpdater.updateScaleInScripts(width, height, fractal.scale, calcScript, bitmapScript)
    }

    private fun updateBitmapInScripts() {
        this.bitmapScript.bind_bitmapData(bitmapAllocation.bitmapData)

        this.calcScript._width = bitmapAllocation.width
        this.calcScript._height = bitmapAllocation.height

        this.bitmapScript._width = bitmapAllocation.width
        this.bitmapScript._height = bitmapAllocation.height

        // must call updateScaleInScripts afterwards!
    }

    private inner class CalcTaskListener: CalculationTask.Listener {
        override fun started() {
            listener?.started()
        }

        override fun updated() {
            notifyUpdated()
            listener?.updated()
        }

        override fun finished() {
            // hide progress bar
            calculationTask = null

            listener?.finished()

            if(changes.isNotEmpty()) {
                val isParameterChange = changes.fold(false) { status, task ->
                    task.execute(this@RenderScriptBitmapModel)
                    status or task.isParameterChange
                }

                changes.clear()

                if(isParameterChange) {
                    startTask()
                }
            }
        }

        override fun progress(progress: Float) {
            listener?.progress(progress)
        }
    }
}