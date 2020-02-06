package at.searles.fractbitmapprovider.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import at.searles.fractbitmapmodel.*
import at.searles.fractimageview.ScalableImageView

class DemoActivity : AppCompatActivity(), BitmapController.Listener, FractBitmapModel.Listener {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var bitmapModelFragment: FractBitmapModelFragment

    private val experimentButton by lazy {
        findViewById<Button>(R.id.triggerExperimentButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.visibility = View.INVISIBLE

        initBitmapModelFragment()

        experimentButton.setOnClickListener {
            if(bitmapModelFragment.bitmapModel.width != 10000) {
                bitmapModelFragment.addImageSizeChange(10000, 6000)
            } else {
                bitmapModelFragment.addImageSizeChange(1000, 600)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectBitmapModelFragment()
    }

    private fun initBitmapModelFragment() {
        val fragment =
            supportFragmentManager.findFragmentByTag(bitmapModelFragmentTag) as FractBitmapModelFragment?

        bitmapModelFragment = fragment ?:
                FractBitmapModelFragment.createInstance(program).also {
                    supportFragmentManager.beginTransaction().add(it, bitmapModelFragmentTag).commit()
                }
    }

    private fun connectBitmapModelFragment() {
        imageView.scalableBitmapModel = bitmapModelFragment.bitmapModel
        imageView.visibility = View.VISIBLE
        imageView.invalidate()

        /*val delay: Long = 1
        val handler = Handler()
        val runnable = object: Runnable {
            override fun run() {
                bitmapModelFragment.bitmapModel.addChange(RelativeScaleChange(Matrix()))
                bitmapModelFragment.addImageSizeChange(100, 100)
                handler.postDelayed(this, delay)
            }
        }

        handler.postDelayed(runnable, delay)*/
    }

    companion object {
        const val bitmapModelFragmentTag = "bitmapModelFragment"
        
        const val program =
            "setResult(0, {var a = 1000001; [cos rad point, sin rad point][a]}, {var a = 1; [cos rad point, sin rad point][a]});" +
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
        imageView.invalidate()
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        // ignore.
    }
}
