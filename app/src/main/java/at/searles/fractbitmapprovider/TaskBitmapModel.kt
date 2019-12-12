package at.searles.fractbitmapprovider

import android.graphics.Matrix
import android.os.Looper
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.PostCalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.RelativeScaleTask
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class TaskBitmapModel(private val calcTaskFactory: CalcTaskFactory): CalculationTask.Listener, ScalableBitmapModel() {

    override val bitmapTransformMatrix = Matrix()

    override val bitmap get() = calcTaskFactory.bitmap

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

            calcTaskFactory.pixelGap = pixelGap
            calcTaskFactory.syncBitmap()

            lastPixelGap = pixelGap
            listener?.bitmapUpdated()
        } else if(lastPixelGap != pixelGap) {
            calcTaskFactory.pixelGap = pixelGap
            calcTaskFactory.syncBitmap()

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
                task.execute(calcTaskFactory)
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
        task.execute(calcTaskFactory)

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

        calculationTask = calcTaskFactory.createCalculationTask().apply {
            listener = this@TaskBitmapModel
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