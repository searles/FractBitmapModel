package at.searles.fractbitmapmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.RenderScript
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.searles.fractbitmapmodel.changes.BitmapAllocationChange
import at.searles.fractbitmapmodel.changes.Change
import at.searles.fractlang.FractlangProgram
import at.searles.paletteeditor.Palette
import org.json.JSONObject

class FractBitmapModelFragment : Fragment(), FractBitmapModel.Listener {

    private lateinit var rs: RenderScript

    lateinit var bitmapModel: FractBitmapModel
        private set

    val bitmap: Bitmap?
        get() = if(isInitializing) null else bitmapModel.bitmap

    val scale
        get() = bitmapModel.scale

    val sourceCode
        get() = bitmapModel.sourceCode

    val parameters
        get() = bitmapModel.parameters

    var palettes
        get() = bitmapModel.palettes
        set(value) {
            bitmapModel.palettes = value
        }

    var shaderProperties
        get() = bitmapModel.shaderProperties
        set(value) {
            bitmapModel.shaderProperties = value
        }

    var isInitializing: Boolean = true
        private set

    var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        asyncInitialize()
    }

    fun addImageSizeChange(width: Int, height: Int) {
        try {
            val newBitmapAllocation = BitmapAllocation(rs, width, height)
            bitmapModel.addChange(BitmapAllocationChange(newBitmapAllocation))
        } catch(th: Throwable) {
            Toast.makeText(context, th.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Applies new bitmap properties
     */
    fun updateBitmap() {
        bitmapModel.updateBitmap()
    }

    fun addChange(change: Change) {
        bitmapModel.addChange(change)
    }

    fun createJson(): JSONObject {
        return bitmapModel.createJson()
    }

    private fun asyncInitialize() {
        // This should be fast enough to not raise problems wrt leaks.
        @SuppressLint("StaticFieldLeak")
        val task = object: AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                val sourceCode = arguments!!.getString(sourceCodeKey)!!

                val program = FractlangProgram(sourceCode, emptyMap())

                rs = RenderScript.create(context)

                val calcProperties = CalcProperties(CalcProperties.getScale(program.scale), program)
                val bitmapProperties = BitmapProperties(CalcProperties.getPalettes(program.palettes), ShaderProperties())

                val bitmapAllocation = BitmapAllocation(rs, 1000,600)

                bitmapModel = FractBitmapModel(rs, bitmapAllocation, calcProperties, bitmapProperties).apply {
                    listener = this@FractBitmapModelFragment
                }
            }

            override fun onPostExecute(result: Unit?) {
                isInitializing = false
                listener?.initializationFinished()
                bitmapModel.startTask()
            }
        }

        task.execute()
    }

    interface Listener: FractBitmapModel.Listener {
        fun initializationFinished()
    }

    override fun started() {
        listener?.started()
    }

    override fun setProgress(progress: Float) {
        listener?.setProgress(progress)
    }

    override fun bitmapUpdated() {
        listener?.bitmapUpdated()
    }

    override fun finished() {
        listener?.finished()
    }

    companion object {
        const val sourceCodeKey = "sourceCode"

        fun createInstance(sourceCode: String): FractBitmapModelFragment {
            val bundle = Bundle().apply {
                putString(sourceCodeKey, sourceCode)
            }

            return FractBitmapModelFragment().apply {
                arguments = bundle
            }
        }
    }
}