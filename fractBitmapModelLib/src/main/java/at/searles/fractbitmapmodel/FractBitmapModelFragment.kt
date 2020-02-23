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
        val width = arguments!!.getInt(widthKey)
        val height = arguments!! .getInt(heightKey)

        val parametersMap = parameters.keySet().map { it to parameters.getString(it)!! }.toMap()

        val properties = FractProperties.create(sourceCode, parametersMap, null, null, emptyList())

        rs = RenderScript.create(context)

        val bitmapAllocation = BitmapAllocation(rs, width, height)

        bitmapModel = FractBitmapModel(rs, bitmapAllocation, properties)
        bitmapModel.startTask()
    }

    companion object {
        const val sourceCodeKey = "sourceCode"
        const val parametersKey = "parameters"
        const val widthKey = "width"
        const val heightKey ="height"

        fun createInstance(sourceCode: String, parameters: Map<String, String>, width: Int, height: Int): FractBitmapModelFragment {
            val parametersBundle = Bundle()

            parameters.forEach { (key, value) ->
                parametersBundle.putString(key, value)
            }

            val bundle = Bundle().apply {
                putString(sourceCodeKey, sourceCode)
                putBundle(parametersKey, parametersBundle)
                putInt(widthKey, width)
                putInt(heightKey, height)
            }


            return FractBitmapModelFragment().apply {
                arguments = bundle
            }
        }
    }
}