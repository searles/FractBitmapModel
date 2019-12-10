package at.searles.fractbitmapprovider.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.renderscript.RenderScript
import at.searles.fractbitmapprovider.RenderScriptBitmapModel
import at.searles.fractimageview.ScalableImageView
import kotlin.math.cos
import kotlin.math.sin

class DemoActivity : AppCompatActivity() {

    val imageView by lazy {
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

        calc()
        startRotation()
    }

    private fun initBitmapModel() {
        bitmapModel = RenderScriptBitmapModel(RenderScript.create(this))

        val memento = bitmapModel.createBitmapMemento(2000,1500)
        bitmapModel.setBitmapMemento(memento)
    }

    private fun calc() {
        bitmapModel.calc()
        bitmapModel.bitmapMemento.syncBitmap()
    }

    var alpha = 0.0

    fun startRotation() {
        val task = object: Runnable {
            override fun run() {
                bitmapModel.setLightVector(0.8f * sin(alpha).toFloat(), 0.8f * cos(alpha).toFloat(), 0.6f)
                alpha += 0.05
                syncBitmap()
                handler.postDelayed(this, 25)
            }
        }

        handler.postDelayed(task, 25)
    }

    private fun syncBitmap() {
        bitmapModel.bitmapMemento.syncBitmap()
        imageView.invalidate()
    }
}
