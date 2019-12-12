#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapprovider)
#pragma rs_fp_relaxed

/*
 * This file performs all calculations. For this purpose, it
 * always calculates a tile of a given size which is then
 * copied into the real data. Another script translates this
 * information into bitmap colors.
 */

/*
 * Float3 has the following structure:
 * xValue, ranging from 0-1, higher value is the palette index.
 * yValue, ranging from 0-1.
 * height.
 * To indicate that a point is outdated, it should be set to
 */
#include "scale.rsh"
#include "complex.rsh"

// dimensions of bitmap. range of data is from 0 to inclusive width!
uint32_t width;
uint32_t height;

// ---------------------------------------------------------------------------------------------- //

static float3 mandelbrot(double2 pt) {
    int32_t maxIterationCount = 1000;
    float bailoutValue = 86.f; // cut off for native_exp
    uint32_t i = 0;
    double2 z = (double2) {0.0, 0.0};
    float sum = 0.f;
    while(i < maxIterationCount) {
        z = csqr(z) + pt;
        float d = fast_length(convert_float2(z));
        if(d > bailoutValue) {
            float value = native_log(sum + 1.f);
            value = value - floor(value);
            return (float3) {value, 0.f, value};
        }
        sum = sum + native_exp(-d);
        i++;
    }
    return (float3) {1.f, 0.f, 0.f};
}

static float3 valueAt(double2 p) {
    float z = sin(sqrt((float) (p.x * p.x + p.y * p.y)));

    float2 fp = convert_float2(p);
    fp = fp - floor(fp);

    return (float3) {z - floor(z), fp.y, z};
}

float3 RS_KERNEL calculate(float3 in, uint32_t x) {
    uint32_t y = x / (width + 1);
    x = x % (width + 1);

    double2 pt = mapCoordinates(x, y);

    float3 value = mandelbrot(pt);
    return value;
}

rs_allocation bitmapData;
uint32_t pixelIndex0;
int ceilLog2Width;
int ceilLog2Height;

static int2 getPixelCoordinates(int index) {
    int x = 0;
    int y = 0;

    for(int a = 0; a < ceilLog2Width - min(ceilLog2Width, ceilLog2Height); a++) {
        x = (x << 1) | ((index >> a) & 1);
    }

    for(int a = 0; a < ceilLog2Height - min(ceilLog2Width, ceilLog2Height); a++) {
        y = (y << 1) | ((index >> a) & 1);
    }

    for(int a = abs(ceilLog2Width - ceilLog2Height); a < ceilLog2Width + ceilLog2Height; a += 2) {
        y = (y << 1) | ((index >> (a + 1)) & 1);
        x = (x << 1) | ((index >> a) & 1);
    }

    return (int2) {x, y};
}


uchar __attribute__((kernel)) calculate_part(uint32_t x) { // name x is mandatory
    uint32_t pixelIndex = pixelIndex0 + x;

    int2 px = getPixelCoordinates(pixelIndex);

    if(px.x > width || px.y > height) {
        return 0;
    }

    double2 pt = mapCoordinates(px.x, px.y);
    float3 value = mandelbrot(pt);

    rsSetElementAt_float3(bitmapData, value, px.y * (width + 1) + px.x);
    return 1;
}