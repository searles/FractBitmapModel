package at.searles.fractbitmapprovider.demo

import android.os.Bundle
import android.renderscript.RenderScript
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import at.searles.fractbitmapmodel.*
import at.searles.fractimageview.ScalableImageView
import at.searles.fractlang.FractlangProgram

class DemoActivity : AppCompatActivity() {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var controller: CalcController
    private lateinit var bitmapModel: CalcBitmapModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBitmapModel()

        imageView.scalableBitmapModel = bitmapModel

        //AnimationTask(calculationTaskFactory).start()
    }

    private fun initBitmapModel() {
        val shaderProperties = ShaderProperties()

        val fractlangProgram = FractlangProgram(program, emptyMap())

        val initialScale = CalcProperties.getScale(fractlangProgram.scale)
        val initialPalettes = CalcProperties.getPalettes(fractlangProgram.palettes)

        val calcProperties = CalcProperties(
            initialScale,
            fractlangProgram
        )

        val bitmapProperties = BitmapProperties(
            initialPalettes,
            shaderProperties
        )

        val rs = RenderScript.create(this)

        val bitmapAllocation = BitmapAllocation(rs, 1000,600)

        controller = CalcController(rs, calcProperties, bitmapProperties, bitmapAllocation)

        controller.bitmapSync.listener = object: BitmapSync.Listener {
            override fun bitmapUpdated() {
                imageView.invalidate()
            }
        }

        bitmapModel = CalcBitmapModel(controller).apply {
            listener = object: CalcBitmapModel.Listener {
                var timerStart: Long = 0

                override fun started() {
                    imageView.invalidate()
                    timerStart = System.currentTimeMillis()
                }

                override fun progress(progress: Float) {
                }

                override fun bitmapUpdated() {
                    imageView.invalidate()
                }

                override fun finished() {
                    imageView.invalidate()
                    Log.d("TIMER", "duration: ${System.currentTimeMillis() - timerStart}")
                }
            }
        }

        bitmapModel.startTask()
    }

    companion object {
        //val program = "setResult(0, cos (arc point + 2), sin rad point);"
        const val program =
            "val z0 = 0:0;\n" +
                    "var c = point;\n" +
                    "var n = 0;\n" +
                    "\n" +
                    "var z = z0;\n" +
                    "\n" +
                    "val bailoutValue = 64;\n" +
                    "val maxExponent = 2;\n" +
                    "val maxIterationCount = 1024;\n" +
                    "\n" +
                    "while ({\n" +
                    "\tz = z^maxExponent + c;\n" +
                    "\t\n" +
                    "\tvar radZ = rad z;\n" +
                    "\t\n" +
                    "\tif(radZ > bailoutValue) {\n" +
                    "\t\tvar continuousAddend = log(log radZ / log bailoutValue) / log maxExponent;\n" +
                    "\t\tvar logN = log(n + 1 - continuousAddend);\n" +
                    "\t\tsetResult(0, logN, logN);\n" +
                    "\t\tfalse\n" +
                    "\t} else if(not next(maxIterationCount, n)) {\n" +
                    "\t\tsetResult(1, 0, log radZ);\n" +
                    "\t\tfalse\n" +
                    "\t} else {\n" +
                    "\t\ttrue\n" +
                    "\t}\n" +
                    "})"
    }
}
