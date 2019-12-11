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
    /**
     * This matrix is used to transform the shown image.
     */
    override val normMatrix: Matrix = Matrix()
    private var nextNormMatrix: Matrix? = null

    override fun scale(m: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        normMatrix.postConcat(m)

        if(nextNormMatrix != null) {
            nextNormMatrix!!.postConcat(m)
        }

        notifyScaleRequested()
    }

    protected abstract fun notifyScaleRequested()

    fun notifyStarted() {
        require(Looper.getMainLooper().isCurrentThread)
        nextNormMatrix = Matrix()
    }

    fun notifyUpdated() {
        if(nextNormMatrix != null) {
            normMatrix.set(nextNormMatrix)
            nextNormMatrix = null
        }
    }
}