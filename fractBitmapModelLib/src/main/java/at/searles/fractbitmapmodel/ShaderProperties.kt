package at.searles.fractbitmapmodel

import android.renderscript.Float3
import kotlin.math.cos
import kotlin.math.sin

class ShaderProperties(
    val useLightEffect: Boolean = true,
    val lightVector: Float3 = Float3(-2f/3f, -2f/3f, -1f/3f),
    val ambientReflection: Float = 0.75f,
    val diffuseReflection: Float = 0.25f,
    val specularReflection: Float = 1f,
    val shininess: Int = 32) {

    fun createWithNewLightVector(polarAngle: Float, azimuthAngle: Float): ShaderProperties {
        return ShaderProperties(useLightEffect, toVector(polarAngle, azimuthAngle),
            ambientReflection, diffuseReflection, specularReflection, shininess
        )
    }

    companion object {
        fun toVector(polarAngle: Float, azimuthAngle: Float): Float3 {
            return Float3(
                sin(polarAngle) * sin(azimuthAngle),
                sin(polarAngle) * cos(azimuthAngle),
                cos(polarAngle)
            )
        }
    }
}