/*
 * Kernel outputs a bitmap that it also takes as input.
 */

#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapprovider)

#include "scale.rsh"
#include "palette.rsh"
#include "lab.rsh"

int width;
int height;

// ---------------------------------------------------------------------------------------------- //

float3 *bitmapData;

float3 lightVector;

static float lambertValue(float3 xVec, float3 yVec) {
    // lambert's law: cosine of angle between normal vector and light vector.
    return dot(normalize(cross(xVec, yVec)), lightVector);
}

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {
    // gather data
    float3 p00 = bitmapData[x + y * (width + 1)];

    float3 p10 = bitmapData[x + y * (width + 1) + 1];
    float3 p01 = bitmapData[x + (y + 1) * (width + 1)];
    float3 p11 = bitmapData[x + (y + 1) * (width + 1) + 1];

    // step 1: obtain color.
    float4 color00 = colorAt(p00.x, p00.y);
    float4 color10 = colorAt(p10.x, p10.y);
    float4 color01 = colorAt(p01.x, p01.y);
    float4 color11 = colorAt(p11.x, p11.y);

    float4 color = (color00 + color10 + color01 + color11) / 4.f;

    // step 2: obtain 3d shade

    float3 xVec = (float3) { scale.a, scale.c, p10.z - p00.z };
    float3 yVec = (float3) { scale.b, scale.d, p01.z - p00.z };

    // for now only gray scale.
    float brightness = lambertValue(xVec, yVec);

    color.s0 *= brightness;

    return rsPackColorTo8888(labToRgb(color));
}