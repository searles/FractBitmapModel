package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import android.renderscript.RenderScript
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.ScalableBitmapModel
import at.searles.paletteeditor.Palette
import org.json.JSONObject

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class FractBitmapModel(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation,
    initialCalcProperties: CalcProperties,
    initialBitmapProperties: BitmapProperties): CalcTask.Listener, ScalableBitmapModel() {

    val sourceCode: String
        get() = calcController.sourceCode

    val parameters
        get() = calcController.parameters

    val scale
        get() = calcController.scale

    var palettes: List<Palette>
        get() = bitmapController.palettes
        set(value) {
            bitmapController.palettes = value
            listener?.propertiesChanged(this)
        }

    var shaderProperties: ShaderProperties
        get() = bitmapController.shaderProperties
        set(value) {
            bitmapController.shaderProperties = value
            listener?.propertiesChanged(this)
        }

    var bitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateScaleInScripts()
            bitmapController.bitmapAllocation = bitmapAllocation
        }

    /*
     * Must be initialized before starting a calculation. This will happen in the
     * first call to startTask.
     */
    private val calcController = CalcController(rs, initialCalcProperties)
    private val bitmapController = BitmapController(rs, initialBitmapProperties, initialBitmapAllocation)

    private var isInitialized: Boolean = false

    override val bitmapTransformMatrix = Matrix()
    override val bitmap get() = bitmapAllocation.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    private var isTaskRunning: Boolean = false
    private var calcTask: CalcTask? = null

    private val postCalcChanges = ArrayList<BitmapModelChange>()
    private var nextCalcProperties: CalcProperties? = null

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        addChange(RelativeScaleChange(relativeMatrix))
    }

    fun updateBitmap() {
        bitmapController.updateBitmap()
    }

    override fun started() {
        // started in listener is already called in startTask
    }

    private var lastPixelGap = -1

    override fun setProgress(progress: Float) {
        if(isWaitingForPreview) {
            bitmapTransformMatrix.set(nextBitmapTransformMatrix)
            isWaitingForPreview = false
            bitmapController.updateBitmap()
            lastPixelGap = bitmapAllocation.pixelGap
            listener?.bitmapUpdated()
        } else if(lastPixelGap != bitmapAllocation.pixelGap) {
            bitmapController.updateBitmap()
            lastPixelGap = bitmapAllocation.pixelGap
            listener?.bitmapUpdated()
        }

        listener?.setProgress(progress)
    }

    override fun finished() {
        isTaskRunning = false
        calcTask = null

        listener?.finished()

        var propertiesChanged = false

        if(nextCalcProperties != null) {
            setCalcProperties(nextCalcProperties!!)
            nextCalcProperties = null

            propertiesChanged = true
        }

        if(postCalcChanges.isNotEmpty()) {
            postCalcChanges.forEach { it.accept(this) }
            postCalcChanges.clear()

            propertiesChanged = true
        }

        if(propertiesChanged) {
            listener?.propertiesChanged(this)
            startTask()
        }
    }

    fun addChange(change: Change) {
        if(isTaskRunning) {
            if(change is CalcPropertiesChange) {
                nextCalcProperties = if (nextCalcProperties == null) {
                    change.accept(calcController.calcProperties)
                } else {
                    change.accept(nextCalcProperties!!)
                }
            }

            if(change is BitmapModelChange) {
                postCalcChanges.add(change)
            }

            calcTask!!.cancel(true)
            return
        }

        require(nextCalcProperties == null)

        if(change is CalcPropertiesChange) {
            setCalcProperties(change.accept(calcController.calcProperties))
        }

        if(change is BitmapModelChange) {
            change.accept(this)
        }

        listener?.propertiesChanged(this)

        startTask()
    }

    private fun setCalcProperties(newCalcProperties: CalcProperties) {
        require(!isTaskRunning)
        calcController.calcProperties = newCalcProperties
        updateScaleInScripts()
    }

    fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(postCalcChanges.isEmpty())
        require(!isTaskRunning)
        require(calcTask == null)

        isWaitingForPreview = true
        isTaskRunning = true

        // before the first preview is generated, we must use the old imageTransformMatrix.
        nextBitmapTransformMatrix.set(null)

        listener?.started()

        if(!isInitialized) {
            // Initialize script the first time this method is called.
            calcController.initialize()
            bitmapController.initialize()
            updateScaleInScripts()
            isInitialized = true
        }

        calcTask = calcController.createCalculationTask(bitmapAllocation).apply {
            listener = this@FractBitmapModel
            execute()
        }
    }

    private fun updateScaleInScripts() {
        val scriptScale =
            calcController.createScriptScale(bitmapAllocation.width, bitmapAllocation.height)

        calcController.setScriptScale(scriptScale)
        bitmapController.setScriptScale(scriptScale)
    }

    fun setBitmapProperties(newBitmapProperties: BitmapProperties) {
        bitmapController.bitmapProperties = newBitmapProperties
    }

    fun createJson(): JSONObject {
        val obj = JSONObject()
        calcController.calcProperties.createJson(obj)
        bitmapController.bitmapProperties.createJson(obj)

        return obj
    }

    interface Listener {
        fun started()
        fun setProgress(progress: Float)
        fun bitmapUpdated()
        fun finished()

        fun propertiesChanged(src: FractBitmapModel)
    }
}