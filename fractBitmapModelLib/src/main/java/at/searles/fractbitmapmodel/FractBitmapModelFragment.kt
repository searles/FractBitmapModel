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

class FractBitmapModelFragment : Fragment() {

    private lateinit var rs: RenderScript

    lateinit var bitmapModel: FractBitmapModel
        private set

    val bitmap: Bitmap?
        get() = if(isInitializing) null else bitmapModel.bitmap

    var isInitializing: Boolean = true
        private set

    var initListener: Listener? = null

    var listener: FractBitmapModel.Listener?
        get() {
            return bitmapModel.listener
        }

        set(value) {
            bitmapModel.listener = value
        }

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

    fun setPalette(index: Int, palette: Palette) {
        bitmapModel.setPalette(index, palette)
    }

    fun addChange(change: Change) {
        bitmapModel.addChange(change)
    }

    private fun asyncInitialize() {
        // This should be fast enough to not raise problems wrt leaks.
        @SuppressLint("StaticFieldLeak")
        val task = object: AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                val sourceCode = arguments!!.getString(sourceCodeKey)!!

                val program = FractlangProgram(sourceCode, emptyMap())

                rs = RenderScript.create(context)

                // TODO maybe there is a better place...
                val calcProperties = CalcProperties(CalcProperties.getScale(program.scale), program)
                val bitmapProperties = BitmapProperties(CalcProperties.getPalettes(program.palettes), ShaderProperties())

                val bitmapAllocation = BitmapAllocation(rs, 1000,600)

                bitmapModel = FractBitmapModel(rs, bitmapAllocation, calcProperties, bitmapProperties)
            }

            override fun onPostExecute(result: Unit?) {
                isInitializing = false
                initListener?.initializationFinished()
                bitmapModel.startTask()
            }
        }

        task.execute()
    }

    interface Listener {
        fun initializationFinished()
    }

    companion object {
        val sourceCodeKey = "sourceCode"

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