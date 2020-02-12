package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties

/**
 * These changes are applied directly to the bitmap. The changes are also applied to
 * fractProperties. This is used eg if the palette offsets are modified. These changes
 * can be applied directly to the bitmap controller
 */
interface BitmapPropertiesChange {
    /**
     * Returns an instance of FractProperties with modified palettes and/or shaderproperties.
     */
    fun accept(properties: FractProperties): FractProperties
}