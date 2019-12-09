package at.searles.fractbitmapprovider

import android.graphics.Matrix
import android.os.Looper
import androidx.core.graphics.values
import at.searles.commons.math.Scale
import at.searles.fractimageview.ScalableBitmapModel

abstract class CoordinatesBitmapModel: ScalableBitmapModel() {
    /**
     * This matrix contains the image tranformation.
     */
    override val normMatrix: Matrix = Matrix() // FIXME name!

    var scale = Scale(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
        private set

    override fun scale(m: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)
        normMatrix.postConcat(m)
        notifyScaleRequested()
    }

    protected abstract fun notifyScaleRequested()

    protected fun popNormMatrix() {
        require(Looper.getMainLooper().isCurrentThread)

        // use the inverse of the normMatrix
        val m = Matrix().also { normMatrix.invert(it) }

        scale = scale.createRelative(Scale.fromMatrix(*m.values()))
        normMatrix.reset()
    }
}