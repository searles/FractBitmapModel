package at.searles.fractbitmapprovider.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.renderscript.Float3
import android.renderscript.RenderScript
import android.util.Log
import android.util.SparseArray
import at.searles.commons.math.Scale
import at.searles.fractbitmapprovider.BitmapAllocation
import at.searles.fractbitmapprovider.RenderScriptBitmapModel
import at.searles.fractbitmapprovider.ScriptsInstance
import at.searles.fractbitmapprovider.fractalbitmapmodel.CalculationTask
import at.searles.fractbitmapprovider.fractalbitmapmodel.Fractal
import at.searles.fractimageview.ScalableImageView
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb
import kotlin.math.cos
import kotlin.math.sin

class DemoActivity : AppCompatActivity() {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var bitmapModel: RenderScriptBitmapModel
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBitmapModel()

        imageView.scalableBitmapModel = bitmapModel
        handler = Handler()

        //startRotation()
    }

    private fun initBitmapModel() {
        val fractal = Fractal(Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0),
            palettes = listOf(
                Palette(5, 2, 0f, 0f,
                    SparseArray<SparseArray<Lab>>().also { table ->
                        table.put(0, SparseArray<Lab>().also { row ->
                            row.put(0, Rgb(0f, 0f, 0f).toLab())
                            row.put(1, Rgb(1f, 0f, 0f).toLab())
                            row.put(2, Rgb(1f, 1f, 0f).toLab())
                            row.put(3, Rgb(1f, 1f, 1f).toLab())
                            row.put(4, Rgb(0f, 0f, 1f).toLab())
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
        )

        val rs = RenderScript.create(this)

        val scripts = ScriptsInstance(rs)
        val bitmapAllocation = BitmapAllocation(2000,1200, scripts)

        bitmapModel = RenderScriptBitmapModel(fractal, bitmapAllocation, scripts).also {
            it.listener = object: CalculationTask.Listener {
                var timerStart: Long = 0
                override fun started() {
                    imageView.invalidate()
                    timerStart = System.currentTimeMillis()
                }

                override fun updated() {
                    imageView.invalidate()
                }

                override fun finished() {
                    imageView.invalidate()
                    Log.d("TIMER", "duration: ${System.currentTimeMillis() - timerStart}")
                }

                override fun progress(progress: Float) {
                }
            }
        }
    }

    var alpha = 0.0f

    fun startRotation() {
        val task = object: Runnable {
            override fun run() {
                bitmapModel.lightVector = Float3(0.8f * sin(alpha), 0.8f * cos(alpha), 0.6f)
                bitmapModel.setPaletteOffset(0, alpha * 0.17f, alpha * 0.03f)
                alpha += 0.05f
                bitmapModel.fastSyncBitmap(rotationResolution)
                imageView.invalidate()
                handler.postDelayed(this, 50)
            }
        }

        handler.postDelayed(task, 25)
    }

    companion object {
        val rotationResolution = 720
    }
}
