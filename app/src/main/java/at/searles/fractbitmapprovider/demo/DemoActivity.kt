package at.searles.fractbitmapprovider.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import at.searles.fractbitmapmodel.*
import at.searles.fractimageview.ScalableImageView

class DemoActivity : AppCompatActivity(), BitmapController.Listener, CalcBitmapModel.Listener, BitmapModelFragment.Listener {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var bitmapModelFragment: BitmapModelFragment

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
            supportFragmentManager.findFragmentByTag(bitmapModelFragmentTag) as BitmapModelFragment?

        bitmapModelFragment = fragment ?:
                BitmapModelFragment.createInstance(program).also {
                    supportFragmentManager.beginTransaction().add(it, bitmapModelFragmentTag).commit()
                }

        bitmapModelFragment.initListener = this
    }

    override fun initializationFinished() {
        connectBitmapModelFragment()
    }

    private fun connectBitmapModelFragment() {
        imageView.scalableBitmapModel = bitmapModelFragment.bitmapModel
        imageView.visibility = View.VISIBLE
        imageView.invalidate()

        bitmapModelFragment.listener = this
    }

    companion object {
        const val bitmapModelFragmentTag = "bitmapModelFragment"
        
        val program =
            "extern addend: \"Addend\" = \"0.1\";" +
            "setResult(0, cos (arc point), sin rad point);" +
            "declareScale(5,0,0,5,0,0);" +
            "declarePalette(\"1\", 2, 2, [1,1,#ffff0000], [0,0,#ff0000ff]);"
    }

    override fun started() {
        Log.d("DemoActivity", "started")
    }

    override fun progress(progress: Float) {
        Log.d("DemoActivity", "progress: $progress")
    }

    override fun bitmapUpdated() {
        imageView.invalidate()
    }

    override fun finished() {
        Log.d("DemoActivity", "finished")
    }
}
