float3 lightVector;

double xStepLength;
double yStepLength;

float aNorm; // = scale.a / |(scale.a, scale.c)|
float bNorm;
float cNorm;
float dNorm;

static float lambertValue(float3 xVec, float3 yVec) {
    // lambert's law: cosine of angle between normal vector and light vector.
    return dot(fast_normalize(cross(xVec, yVec)), lightVector);
}

static float lightBrightness(double dzx, double dzy) {
    float3 xVec = (float3) { aNorm, cNorm, (float) (dzx / xStepLength) };
    float3 yVec = (float3) { bNorm, dNorm, (float) (dzy / yStepLength) };

    return lambertValue(xVec, yVec);
}

