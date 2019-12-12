package at.searles.fractbitmapprovider

import android.renderscript.RenderScript

class ScriptsInstance(val rs: RenderScript) {
    val calcScript: ScriptC_calc = ScriptC_calc(rs)
    val bitmapScript: ScriptC_bitmap = ScriptC_bitmap(rs)
    val interpolateGapsScript = ScriptC_interpolate_gaps(rs)
}