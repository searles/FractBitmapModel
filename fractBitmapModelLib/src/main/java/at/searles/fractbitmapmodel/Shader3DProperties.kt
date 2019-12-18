package at.searles.fractbitmapmodel

import android.renderscript.Float3
import kotlin.math.cos
import kotlin.math.sin

class Shader3DProperties {
    var useLightEffect: Boolean = true
    var lightVector: Float3 = Float3(-2f/3f, -2f/3f, 1f/3f)
    var ambientReflection = 0f
    var diffuseReflection = 1f
    var specularReflection = 1f
    var shininess: Int = 4

    fun setLightVector(polarAngle: Float, azimuthAngle: Float) {
        lightVector = Float3(
            sin(polarAngle) * sin(azimuthAngle),
            sin(polarAngle) * cos(azimuthAngle),
            cos(polarAngle)
        )
    }
}