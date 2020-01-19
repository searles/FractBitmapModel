package at.searles.fractbitmapmodel

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.RenderScript
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.searles.fractbitmapmodel.changes.BitmapAllocationChange
import at.searles.fractlang.FractlangProgram

class FractBitmapModelFragment : Fragment() {

    private lateinit var rs: RenderScript

    lateinit var bitmapModel: FractBitmapModel
        private set

    var isInitializing: Boolean = true
        private set

    var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        asyncInitialize()

        // TODO: Dimensions as parameter
    }

    fun addImageSizeChange(width: Int, height: Int) {
        try {
            val newBitmapAllocation = BitmapAllocation(rs, width, height)
            bitmapModel.addChange(BitmapAllocationChange(newBitmapAllocation))
        } catch(th: Throwable) {
            Toast.makeText(context, th.localizedMessage, Toast.LENGTH_LONG).show()
        }
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

                val bitmapAllocation = BitmapAllocation(rs, defaultWidth, defaultHeight)

                bitmapModel = FractBitmapModel(rs, bitmapAllocation, calcProperties, bitmapProperties)
            }

            override fun onPostExecute(result: Unit?) {
                isInitializing = false
                listener?.initializationFinished()
                bitmapModel.startTask()
            }
        }

        task.execute()
    }

    interface Listener {
        fun initializationFinished()
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

        const val defaultWidth = 1024
        const val defaultHeight = 768
    }
}