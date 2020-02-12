package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import android.renderscript.RenderScript
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class FractBitmapModel(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation,
    initialProperties: FractProperties): CalcTask.Listener, ScalableBitmapModel() {

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
    private val calcController = CalcController(rs, initialProperties)
    private val bitmapController = BitmapController(rs, initialBitmapAllocation)

    private var isInitialized: Boolean = false

    override val bitmapTransformMatrix = Matrix()
    override val bitmap get() = bitmapAllocation.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    private var isTaskRunning: Boolean = false
    private var calcTask: CalcTask? = null

    private val bitmapModelChanges = ArrayList<BitmapModelChange>()

    val properties: FractProperties
        get() = calcController.properties

    private var nextProperties: FractProperties? = null

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        scheduleCalcPropertiesChange(RelativeScaleChange(relativeMatrix))
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

        if(nextProperties != null) {
            setProperties(nextProperties!!)
            nextProperties = null

            propertiesChanged = true
        }

        if(bitmapModelChanges.isNotEmpty()) {
            bitmapModelChanges.forEach { it.accept(this) }
            bitmapModelChanges.clear()

            propertiesChanged = true
        }

        if(propertiesChanged) {
            listener?.propertiesChanged(this)
            startTask()
        }
    }

    fun applyBitmapPropertiesChange(change: BitmapPropertiesChange) {
        require(Looper.getMainLooper().isCurrentThread)

        val bitmapProperties =
            if(nextProperties != null) {
                nextProperties = change.accept(nextProperties!!)
                nextProperties!!
            } else {
                calcController.properties = change.accept(calcController.properties)
                calcController.properties
            }

        bitmapController.updateShaderProperties(bitmapProperties)
        bitmapController.updatePalettes(bitmapProperties)
        bitmapController.updateBitmap(change.useFastRoot)
    }

    fun scheduleCalcPropertiesChange(change: CalcPropertiesChange) {
        require(Looper.getMainLooper().isCurrentThread)

        val currentProperties = nextProperties ?: calcController.properties

        if(isTaskRunning) {
            nextProperties = change.accept(currentProperties)
            calcTask!!.cancel(true)
            return
        }

        require(nextProperties == null)
        setProperties(change.accept(calcController.properties))
        listener?.propertiesChanged(this)

        startTask()
    }

    fun scheduleBitmapModelChange(change: BitmapModelChange) {
        if(isTaskRunning) {
            bitmapModelChanges.add(change)
            calcTask!!.cancel(true)
            return
        }

        change.accept(this)
        // TODO update scale?
        listener?.propertiesChanged(this)
        startTask()
    }

    /**
     * Sets new properties
     */
    private fun setProperties(newProperties: FractProperties) {
        require(!isTaskRunning)
        calcController.properties = newProperties
        bitmapController.updatePalettes(newProperties)
        bitmapController.updatePalettes(newProperties)
        calcController.updateVmCodeInScript()
        updateScaleInScripts()
    }

    fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(bitmapModelChanges.isEmpty())
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
            bitmapController.initialize(properties)
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

    interface Listener {
        fun started()
        fun setProgress(progress: Float)
        fun bitmapUpdated()
        fun finished()

        fun propertiesChanged(src: FractBitmapModel)
    }
}