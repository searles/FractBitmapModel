package at.searles.fractbitmapprovider.demo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import at.searles.fractbitmapmodel.*
import at.searles.fractimageview.ScalableImageView

class DemoActivity : AppCompatActivity(), BitmapController.Listener, FractBitmapModelFragment.Listener, FractBitmapModel.Listener {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    // TODO Create bitmapModelFragment right away.
    private lateinit var bitmapModelFragment: FractBitmapModelFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.visibility = View.INVISIBLE

        initBitmapModelFragment()
    }

    override fun onResume() {
        super.onResume()

        if(!bitmapModelFragment.isInitializing) {
            connectBitmapModelFragment()
        }
    }

    private fun initBitmapModelFragment() {
        val fragment =
            supportFragmentManager.findFragmentByTag(bitmapModelFragmentTag) as FractBitmapModelFragment?

        bitmapModelFragment = fragment ?:
                FractBitmapModelFragment.createInstance(program).also {
                    supportFragmentManager.beginTransaction().add(it, bitmapModelFragmentTag).commit()
                }

        bitmapModelFragment.listener = this
    }

    override fun initializationFinished() {
        connectBitmapModelFragment()
    }

    private fun connectBitmapModelFragment() {
        imageView.scalableBitmapModel = bitmapModelFragment.bitmapModel
        imageView.visibility = View.VISIBLE
        imageView.invalidate()

        /*
        XXX Trigger lots of changes in a short amount of time
        val delay: Long = 500
        val handler = Handler()
        val runnable = object: Runnable {
            override fun run() {
                bitmapModelFragment.addImageSizeChange(5080, 6000)
                handler.postDelayed(this, delay)
            }
        }

        handler.postDelayed(runnable, delay)
         */
    }

    companion object {
        const val bitmapModelFragmentTag = "bitmapModelFragment"
        
        const val program =
            "extern fn: \"Fn\" = \"exp point\";" +
            "var t = fn;" +
            "setResult(0, arc t / tau, rad t);" +
            "declareScale(5,0,0,5,0,0);" +
            "declarePalette(\"1\", 4, 1, [0,0,#ffff0000], [1,0,#ffffff00], [2,0,#ff00ff00], [3,0,#ff0000ff]);"
    }

    override fun started() {
        Log.d("DemoActivity", "started")
    }

    override fun setProgress(progress: Float) {
        Log.d("DemoActivity", "progress: $progress")
    }

    override fun bitmapUpdated() {
        imageView.invalidate()
    }

    override fun finished() {
        Log.d("DemoActivity", "finished")
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        // ignore.
    }
}
