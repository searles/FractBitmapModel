package at.searles.fractbitmapmodel

import android.renderscript.Float3
import kotlin.math.cos
import kotlin.math.sin

class Shader3DProperties {
    var useLightEffect: Boolean = true
    var lightVector: Float3 = Float3(-1f/3f, -2f/3f, -2f/3f)
    var ambientReflection = 0.5f
    var diffuseReflection = 0.5f
    var specularReflection = 1f
    var shininess: Int = 4 // FIXME this ain't workin'

    fun setLightVector(polarAngle: Float, azimuthAngle: Float) {
        lightVector = Float3(
            sin(polarAngle) * sin(azimuthAngle),
            sin(polarAngle) * cos(azimuthAngle),
            -cos(polarAngle)
        )
    }
}