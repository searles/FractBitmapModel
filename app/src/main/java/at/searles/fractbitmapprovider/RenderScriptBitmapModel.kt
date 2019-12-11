package at.searles.fractbitmapprovider

import android.graphics.Bitmap
import android.renderscript.*
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationChange
import at.searles.fractbitmapprovider.fractalbitmapmodel.ScaleChange
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractbitmapprovider.palette.PaletteWrapper
import kotlin.math.hypot

class RenderScriptBitmapModel(val rs: RenderScript, initialFractal: Fractal): CoordinatesBitmapModel() {

    var listener: CalculationTask.Listener? = null

    private val calcScript: ScriptC_calc = ScriptC_calc(rs)
    private val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)

    override val bitmap: Bitmap
        get() = bitmapMemento.bitmap

    var fractal = initialFractal

    val paletteWrapper = PaletteWrapper(rs, bitmapScript).apply {
        palettes = fractal.palettes
        this.updatePalettes()
    }

    lateinit var bitmapMemento: BitmapAllocation
        private set

    fun registerChange(change: CalculationChange) {
        if(calculationTask == null) {
            require(changes.isEmpty())
            fractal = change.applyChange(fractal)
            startTask()
            return
        }

        changes.add(change)
        calculationTask!!.cancel(true)
    }


    override fun notifyScaleRequested() {
        registerChange(ScaleChange { normMatrix })
    }

    private var calculationTask: CalculationTask? = null
    private val changes = ArrayList<CalculationChange>()

    private val taskListener = object: CalculationTask.Listener {
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
                fractal = changes.fold(fractal) { fractal, change -> change.applyChange(fractal) }
                changes.clear()
                startTask()
            }
        }

        override fun progress(progress: Float) {
        }
    }

    /**
     * Converts the current scale to image coordinates and
     * sets it in renderscript. calc must be called afterwards.
     */
    private fun updateScaleInScripts() {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val factor = 1.0 / if (centerX < centerY) centerX else centerY

        val scale = fractal.scale

        val scaleInScript = ScriptField_Scale.Item().apply {
            a = scale.xx * factor
            b = scale.yx * factor
            c = scale.xy * factor
            d = scale.yy * factor

            e = scale.cx - (a * centerX + b * centerY)
            f = scale.cy - (c * centerX + d * centerY)
        }

        calcScript._scale = scaleInScript

        bitmapScript._scale = scaleInScript

        val xStepLength = hypot(scaleInScript.a, scaleInScript.c)
        val yStepLength = hypot(scaleInScript.b, scaleInScript.d)

        bitmapScript._xStepLength = xStepLength
        bitmapScript._yStepLength = yStepLength

        bitmapScript._aNorm = (scaleInScript.a / xStepLength).toFloat()
        bitmapScript._bNorm = (scaleInScript.b / yStepLength).toFloat()
        bitmapScript._cNorm = (scaleInScript.c / xStepLength).toFloat()
        bitmapScript._dNorm = (scaleInScript.d / yStepLength).toFloat()
    }

    private fun startTask() {
        require(changes.isEmpty())
        require(calculationTask == null)

        // after an update is generated, all scales to this point have been committed.
        notifyStarted()

        updateScaleInScripts() // fixme this should be somewhere else...

        calculationTask = CalculationTask(calcScript).also {
            it.listener = taskListener
        }

        calculationTask!!.execute(bitmapMemento)
    }

    /**
     * Update lightVector. syncBitmap must be called afterwards.
     */
    private val lightVector: Float3 = Float3(1f, 0f, 0f).also {
        bitmapScript._lightVector = it
    }

    fun setLightVector(x: Float, y: Float, z: Float) {
        lightVector.x = x
        lightVector.y = y
        lightVector.z = z

        bitmapScript._lightVector = lightVector
    }

    /**
     * Performs the calculation.
     */

    fun createBitmapMemento(width: Int, height: Int): BitmapAllocation {
        return BitmapAllocation(rs, bitmapScript, width, height)
    }

    fun setBitmapMemento(value: BitmapAllocation) {
        bitmapMemento = value
        this.bitmapScript.bind_bitmapData(value.bitmapData)

        this.calcScript._width = value.width
        this.calcScript._height = value.height

        this.bitmapScript._width = value.width
        this.bitmapScript._height = value.height

        updateScaleInScripts()
        startTask() //  FIXME this should also be a change.
    }
}