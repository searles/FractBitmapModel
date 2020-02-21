package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.searles.commons.util.History
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

    val renderSegment: Allocation = Allocation.createSized(rs, Element.F32_3(rs),
        CalcController.parallelCalculationsCount
    )


    /*
     * Must be initialized before starting a calculation. This will happen in the
     * first call to startTask.
     */
    private val calcController = CalcController(rs, initialProperties)
    private val bitmapController = BitmapController(rs, initialBitmapAllocation)

    override val bitmapTransformMatrix = Matrix()
    override val bitmap get() = bitmapAllocation.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    var isTaskRunning: Boolean = false
        private set

    private var calcTask: CalcTask? = null

    private val bitmapModelChanges = ArrayList<BitmapModelChange>()

    val properties: FractProperties
        get() = calcController.properties

    private var nextProperties: FractProperties? = null

    private val history = History<FractProperties>().apply {
        add(initialProperties)
    }

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        scheduleCalcPropertiesChange(RelativeScaleChange(relativeMatrix))
    }

    fun updateBitmap() {
        bitmapController.updateBitmap()
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
            setProperties(nextProperties!!) // FIXME shouldnt this be calc.properties?
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

        // otherwise they will be updated anyways when initialization is finished
        updateBitmapParametersInScripts(bitmapProperties)
        bitmapController.updateBitmap(change.useFastRoot)
    }

    fun scheduleCalcPropertiesChange(change: CalcPropertiesChange) {
        require(Looper.getMainLooper().isCurrentThread)
        require(nextProperties == null || isTaskRunning)

        val currentProperties = nextProperties ?: calcController.properties

        val changedProperties = change.accept(currentProperties)

        if(change.addToHistory) {
            history.add(changedProperties)
        }

        if(isTaskRunning) {
            nextProperties = changedProperties
            calcTask!!.cancel(true)
            return
        }

        setProperties(changedProperties)
        listener?.propertiesChanged(this)

        startTask()
    }

    fun scheduleBitmapModelChange(change: BitmapModelChange) {
        if(isTaskRunning) {
            bitmapModelChanges.add(change)
            // FIXME
            calcTask!!.cancel(true)
            return
        }

        change.accept(this)

        listener?.propertiesChanged(this)
        startTask()
    }

    fun hasBackHistory() = history.hasBack()

    fun hasForwardHistory() = history.hasForward()

    fun historyBack() {
        scheduleCalcPropertiesChange(NewFractPropertiesChange(history.back(), false))
    }

    fun historyForward() {
        scheduleCalcPropertiesChange(NewFractPropertiesChange(history.forward(), false))
    }

    /**
     * Sets new properties
     */
    private fun setProperties(newProperties: FractProperties) {
        require(!isTaskRunning)
        calcController.properties = newProperties
        calcController.updateVmCodeInScript()
        updateScaleInScripts()
        updateBitmapParametersInScripts(properties)
        bitmapController.updateBitmap(false)
    }

    private fun updateBitmapParametersInScripts(newProperties: FractProperties) {
        // Cannot use 'properties' because they might be only set
        // once the calculation is done.
        bitmapController.updateShaderProperties(newProperties)
        bitmapController.updatePalettes(newProperties)
    }

    private var isInitialized = false

    fun initialize() {
        if(!isInitialized) {
            isInitialized = true

            bitmapController.initialize(properties)
            calcController.initialize()

            // Initialize script the first time this method is called.
            updateScaleInScripts()
        }
    }

    /**
     * Synchronizes scale value between bitmap scripts and calc script
     */
    fun updateScaleInScripts() {
        calcController.updateScale(bitmapAllocation.width, bitmapAllocation.height)
    }

    fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(bitmapModelChanges.isEmpty())
        require(!isTaskRunning)
        require(calcTask == null)

        // Initialize/Reset values
        isWaitingForPreview = true
        isTaskRunning = true

        // before the first preview is generated, we must use the old imageTransformMatrix.
        nextBitmapTransformMatrix.set(null)

        // Now, we start.
        listener?.started()

        startCalcTask()
    }

    val calcScript: ScriptC_calc
        get() = calcController.calcScript

    private fun startCalcTask() {
        calcTask = CalcTask(rs, this).apply {
            listener = this@FractBitmapModel
            execute()
        }
    }

    interface Listener {
        fun started()
        fun setProgress(progress: Float)
        fun bitmapUpdated()
        fun finished()

        fun propertiesChanged(src: FractBitmapModel)
    }
}