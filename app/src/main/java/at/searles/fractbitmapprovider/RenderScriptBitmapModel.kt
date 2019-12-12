package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapprovider.fractalbitmapmodel.PostCalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.RelativeScaleTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractbitmapprovider.palette.PaletteUpdater
import kotlin.math.max

class RenderScriptBitmapModel(initialFractal: Fractal, initialBitmapAllocation: BitmapAllocation, scripts: ScriptsInstance): LongRunningBitmapModel() {

    var listener: CalculationTask.Listener? = null

    private val rs: RenderScript = scripts.rs
    private val calcScript: ScriptC_calc = scripts.calcScript
    private val bitmapScript: ScriptC_bitmap = scripts.bitmapScript
    private val interpolateGapsScript = scripts.interpolateGapsScript

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

    var lightVector: Float3 = Float3(2f/3f, 2f/3f, 1f/3f)
        set(value) {
            field = value
            updateLightParametersInScripts()
        }

    init {
        updateBitmapInScripts()
        updateScaleInScripts()
        updatePalettesInScripts()
        updateLightParametersInScripts()
    }

    private var calculationTask: CalculationTask? = null
    private val changes = ArrayList<PostCalculationTask>()

    override fun notifyScaleRequested() {
        registerPostCalcTask(RelativeScaleTask())
    }

    fun setPaletteOffset(index: Int, offsetX: Float, offsetY: Float) {
        PaletteUpdater(rs, bitmapScript).updateOffsets(index, offsetX, offsetY)
    }

    fun fastSyncBitmap(resolution: Int) {
        // color cycling should happen at a lower resolution.
        bitmapAllocation.fastSyncBitmap(max(1, max(width, height) / resolution))
    }

    fun syncBitmap() {
        bitmapAllocation.syncBitmap()
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

        calculationTask = CalculationTask(rs, calcScript, bitmapScript, interpolateGapsScript, bitmapAllocation, taskListener)
        calculationTask!!.execute()
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

        this.calcScript._width = bitmapAllocation.width.toLong()
        this.calcScript._height = bitmapAllocation.height.toLong()

        this.bitmapScript._width = bitmapAllocation.width.toLong()
        this.bitmapScript._height = bitmapAllocation.height.toLong()

        this.interpolateGapsScript._width = bitmapAllocation.width.toLong()
        this.interpolateGapsScript._height = bitmapAllocation.height.toLong()
        // must call updateScaleInScripts afterwards!
    }

    private fun updateLightParametersInScripts() {
        bitmapScript._lightVector = lightVector
        bitmapScript._ambientLight = 0.1f // FIXME extract
        bitmapScript._diffuseLight = 1f
        bitmapScript._specularStrength = 1f
        bitmapScript._viewerVector = Float3(1f / 3f, 2f / 3f, 2f / 3f)
        bitmapScript._shininess = 4
        bitmapScript._useLightEffect = 1
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