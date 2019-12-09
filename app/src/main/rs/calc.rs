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

int width; // width of bitmap. range of x is 0 to width.
int height;

static float3 valueAt(double2 p) {
    return (float3) {0, 0, sin(sqrt((float) (p.x * p.x + p.y * p.y)))};
}

float3 __attribute__((kernel)) calculate(float3 in, uint32_t x) {
    /*float3 out = in;

    if(out.x >= 0) {
        return out;
    }*/

    uint32_t y = x / (width + 1);
    x = x % (width + 1);

    double2 pt = (double2) { 2.0 * x - (double) width, 2.0 * y - (double) height } / (double2) { width, height };
    float3 value = valueAt(12.0 * pt);
    return value;
}

/*

TODOs:

Kernel:
    - Step 1: Does the point already exist?
    - Step 2: If not, start calculate.

Split image in squares of size NxN.

Step 1: Find smallest i s.t. max(width, height) < N^(2^i)



*/