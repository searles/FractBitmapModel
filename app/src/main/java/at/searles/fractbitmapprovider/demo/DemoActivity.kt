package at.searles.fractbitmapprovider.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.RenderScript
import android.util.Log
import android.util.SparseArray
import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.*
import at.searles.fractlang.CompilerInstance
import at.searles.fractbitmapmodel.tasks.BitmapModelParameters
import at.searles.fractimageview.ScalableImageView
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb

class DemoActivity : AppCompatActivity() {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var calculationTaskFactory: CalculationTaskFactory
    private lateinit var bitmapModel: CalculationTaskBitmapModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBitmapModel()

        imageView.scalableBitmapModel = bitmapModel

        //AnimationTask(calculationTaskFactory).start()
    }

    private fun initBitmapModel() {
        val palettes = listOf(
            Palette(5, 2, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(1, Rgb(0f, 0f, 0f).toLab())
                        row.put(2, Rgb(1f, 0f, 0f).toLab())
                        row.put(3, Rgb(1f, 1f, 0f).toLab())
                        row.put(4, Rgb(1f, 1f, 1f).toLab())
                        row.put(0, Rgb(0f, 0f, 1f).toLab())
                    })
                    table.put(1, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(1f, 1f, 1f).toLab())
                        row.put(1, Rgb(0f, 0.5f, 0f).toLab())
                        row.put(2, Rgb(0f, 0.25f, 1f).toLab())
                        row.put(3, Rgb(0.5f, 0.12f, 0.05f).toLab())
                        row.put(4, Rgb(0f, 0f, 0f).toLab())
                    })
                }),
            Palette(1, 1, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(0f, 0f, 0f).toLab())
                    })
                })
        )

        val shaderProperties = Shader3DProperties()

        val compilerInstance = CompilerInstance(
            program,
            emptyMap()
        ).apply {
            compile()
        }

        val fractal = BitmapModelParameters(Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0),
            palettes,
            shaderProperties,
            compilerInstance
        )

        val rs = RenderScript.create(this)

        val bitmapAllocation = BitmapAllocation(rs, 1000,600)

        calculationTaskFactory = CalculationTaskFactory(rs, fractal, bitmapAllocation)

        calculationTaskFactory.listener = object: CalculationTaskFactory.Listener {
            override fun bitmapSynced() {
                imageView.invalidate()
            }
        }

        bitmapModel = CalculationTaskBitmapModel(calculationTaskFactory).apply {
            listener = object: CalculationTaskBitmapModel.Listener {
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
    }

    companion object {
        //val program = "setResult(0, cos (arc point + 2), sin rad point);"
        val program =
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
