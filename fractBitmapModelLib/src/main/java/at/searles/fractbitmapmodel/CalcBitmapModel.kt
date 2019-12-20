package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class CalcBitmapModel(private val controller: CalcController): CalculationTask.Listener, ScalableBitmapModel() {
    override val bitmapTransformMatrix = Matrix()

    override val bitmap get() = controller.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    private var isTaskRunning: Boolean = false
    private var calculationTask: CalculationTask? = null

    private val postCalcChanges = ArrayList<ControllerChange>()

    private var nextCalcProperties: CalcProperties? = null

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        addChange(RelativeScaleChange(relativeMatrix))
    }

    override fun started() {
        require(nextBitmapTransformMatrix.isIdentity)
        listener?.started()
    }

    private var lastPixelGap = -1

    override fun progress(progress: Float, pixelGap: Int) {
        if(isWaitingForPreview) {
            bitmapTransformMatrix.set(nextBitmapTransformMatrix)
            isWaitingForPreview = false

            controller.bitmapSync.pixelGap = pixelGap
            controller.updateBitmap()

            lastPixelGap = pixelGap
            listener?.bitmapUpdated()
        } else if(lastPixelGap != pixelGap) {
            controller.bitmapSync.pixelGap = pixelGap
            controller.updateBitmap()

            lastPixelGap = pixelGap
            listener?.bitmapUpdated()
        }

        listener?.progress(progress)
    }

    override fun finished() {
        isTaskRunning = false
        calculationTask = null

        listener?.finished()

        var mustStartTask = false

        if(nextCalcProperties != null) {
            controller.calcProperties = nextCalcProperties!!
            nextCalcProperties = null

            mustStartTask = true
        }

        if(postCalcChanges.isNotEmpty()) {
            postCalcChanges.forEach { it.accept(controller) }
            postCalcChanges.clear()

            mustStartTask = true
        }

        if(mustStartTask) {
            startTask()
        }
    }

    fun addChange(change: Change) {
        if(isTaskRunning) {
            require(calculationTask != null)

            if(change is CalcPropertiesChange) {
                nextCalcProperties = if (nextCalcProperties == null) {
                    change.accept(controller.calcProperties)
                } else {
                    change.accept(nextCalcProperties!!)
                }
            }

            if(change is ControllerChange) {
                postCalcChanges.add(change)
            }

            calculationTask!!.cancel(true)
            return
        }

        require(nextCalcProperties == null)

        if(change is CalcPropertiesChange) {
            controller.calcProperties = change.accept(controller.calcProperties)
        }

        if(change is ControllerChange) {
            change.accept(controller)
        }

        startTask()
    }

    fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(postCalcChanges.isEmpty())
        require(!isTaskRunning)
        require(calculationTask == null)

        isWaitingForPreview = true
        isTaskRunning = true

        // before the first preview is generated, we must use the old imageTransformMatrix.
        nextBitmapTransformMatrix.set(null)

        calculationTask = controller.createCalculationTask().apply {
            listener = this@CalcBitmapModel
            execute()
        }
    }

    interface Listener {
        fun started()
        fun progress(progress: Float)
        fun bitmapUpdated()
        fun finished()
    }
}