package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import at.searles.fractbitmapmodel.tasks.PostCalculationTask
import at.searles.fractbitmapmodel.tasks.RelativeScaleTask
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class CalculationTaskBitmapModel(private val calculationTaskFactory: CalculationTaskFactory): CalculationTask.Listener, ScalableBitmapModel() {

    override val bitmapTransformMatrix = Matrix()

    override val bitmap get() = calculationTaskFactory.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    private var isTaskRunning: Boolean = false
    private var calculationTask: CalculationTask? = null
    private val postCalculationTasks = ArrayList<PostCalculationTask>()

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        addPostCalcTask(RelativeScaleTask(relativeMatrix))
    }

    override fun started() {
        require(nextBitmapTransformMatrix.isIdentity)
        listener?.started()
    }

    private var lastPixelGap = -1

    init {
        startTask()
    }

    override fun progress(progress: Float, pixelGap: Int) {
        if(isWaitingForPreview) {
            bitmapTransformMatrix.set(nextBitmapTransformMatrix)
            isWaitingForPreview = false

            calculationTaskFactory.pixelGap = pixelGap
            calculationTaskFactory.syncBitmap()

            lastPixelGap = pixelGap
            listener?.bitmapUpdated()
        } else if(lastPixelGap != pixelGap) {
            calculationTaskFactory.pixelGap = pixelGap
            calculationTaskFactory.syncBitmap()

            lastPixelGap = pixelGap
            listener?.bitmapUpdated()
        }

        listener?.progress(progress)
    }

    override fun finished() {
        // hide progress bar
        isTaskRunning = false
        calculationTask = null

        if(postCalculationTasks.isNotEmpty()) {
            val isParameterChange = postCalculationTasks.fold(false) { status, task ->
                task.execute(calculationTaskFactory)
                status or task.isParameterChange
            }

            postCalculationTasks.clear()

            if(isParameterChange) {
                startTask()
            }
        }
    }

    private fun addPostCalcTask(task: PostCalculationTask) {
        require(Looper.getMainLooper().isCurrentThread)

        if(isTaskRunning) {
            // TODO allow tasks that do not cancel the calculation, eg save image.
            postCalculationTasks.add(task)
            calculationTask!!.cancel(true)
            return
        }

        require(postCalculationTasks.isEmpty())
        task.execute(calculationTaskFactory)

        if(task.isParameterChange) {
            startTask()
        }

        return
    }

    private fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(postCalculationTasks.isEmpty())
        require(!isTaskRunning)
        require(calculationTask == null)

        isWaitingForPreview = true
        isTaskRunning = true

        // before the first preview is generated, we must use the old imageTransformMatrix.
        nextBitmapTransformMatrix.set(null)

        calculationTask = calculationTaskFactory.createCalculationTask().apply {
            listener = this@CalculationTaskBitmapModel
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