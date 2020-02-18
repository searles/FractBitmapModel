package at.searles.fractbitmapmodel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        initialize()

        // TODO: Dimensions as parameter

        if(savedInstanceState != null) {
            // TODO Deserialize fractal
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO: Save it.
    }

    fun addImageSizeChange(width: Int, height: Int) {
        try {
            val newBitmapAllocation = BitmapAllocation(rs, width, height)
            bitmapModel.scheduleBitmapModelChange(BitmapAllocationChange(newBitmapAllocation))
        } catch(th: Throwable) {
            Toast.makeText(context, th.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun initialize() {
        val sourceCode = arguments!!.getString(sourceCodeKey)!!
        val parameters = arguments!!.getBundle(parametersKey)!!

        val parametersMap = parameters.keySet().map { it to parameters.getString(it)!! }.toMap()

        val properties = FractProperties.create(sourceCode, parametersMap, null, null, emptyList())

        rs = RenderScript.create(context)

        val bitmapAllocation = BitmapAllocation(rs, defaultWidth, defaultHeight)

        bitmapModel = FractBitmapModel(rs, bitmapAllocation, properties)
        bitmapModel.startTask()
    }

    companion object {
        const val sourceCodeKey = "sourceCode"
        const val parametersKey = "parameters"

        fun createInstance(sourceCode: String, parameters: Map<String, String>): FractBitmapModelFragment {
            val bundle = Bundle().apply {
                putString(sourceCodeKey, sourceCode)
            }

            val parametersBundle = Bundle()

            parameters.forEach{(key, value) -> parametersBundle.putString(key, value)}

            return FractBitmapModelFragment().apply {
                arguments = bundle
            }
        }

        const val defaultWidth = 1024
        const val defaultHeight = 600 // FIXME
    }
}