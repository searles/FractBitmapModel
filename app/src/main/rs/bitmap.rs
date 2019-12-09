/*
 * Kernel outputs a bitmap that it also takes as input.
 */

#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapprovider)

int width;
int height;

float3 *bitmapData;

float3 lightVector;

static float lambertValue(float3 xVec, float3 yVec) {
    // lambert's law: cosine of angle between normal vector and light vector.
    return dot(normalize(cross(xVec, yVec)), lightVector);
}

static uchar4 gray(float brightness) {
    brightness = brightness * 127 + 127;

    if(brightness > 255) brightness = 255;

    uchar value = (uchar) brightness;

    return (uchar4) {value, value, value, 0xff};
}

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {
    // gather data
    float3 p00 = bitmapData[x + y * (width + 1)];

    float3 p10 = bitmapData[x + y * (width + 1) + 1];
    float3 p01 = bitmapData[x + (y + 1) * (width + 1)];
    float3 p11 = bitmapData[x + (y + 1) * (width + 1) + 1];

    float3 xVec = (float3) { 0.1, 0, p10.z - p00.z };
    float3 yVec = (float3) { 0, 0.1, p01.z - p00.z };

    // for now only gray scale.
    float brightness = lambertValue(xVec, yVec);

    return gray(brightness);
}