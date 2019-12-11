package at.searles.fractbitmapprovider

import android.graphics.Matrix
import android.os.Looper
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
abstract class LongRunningBitmapModel: ScalableBitmapModel() {
    override val normMatrix: Matrix
            get() = imageTransformMatrix // FIXME rename in parent!

    private val imageTransformMatrix = Matrix()
    protected var nextImageTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    override fun scale(relativeImageTransformMatrix: Matrix) { // FIXME rename in parent.
        require(Looper.getMainLooper().isCurrentThread)

        imageTransformMatrix.postConcat(relativeImageTransformMatrix)
        nextImageTransformMatrix.postConcat(relativeImageTransformMatrix)

        notifyScaleRequested()
    }

    fun popImageTransformMatrix(): Matrix {
        val m = nextImageTransformMatrix
        nextImageTransformMatrix = Matrix()
        return m
    }

    protected abstract fun notifyScaleRequested()

    protected fun notifyStarted() {
        require(Looper.getMainLooper().isCurrentThread)
        require(nextImageTransformMatrix.isIdentity)

        isWaitingForPreview = true
    }

    protected fun notifyUpdated() {
        if(isWaitingForPreview) {
            imageTransformMatrix.set(nextImageTransformMatrix)
            isWaitingForPreview = false
        }
    }
}