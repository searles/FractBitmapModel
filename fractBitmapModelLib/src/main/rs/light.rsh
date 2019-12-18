// parameters:
float3 lightVector;
float ambientReflection;
float diffuseReflection;

float specularReflection;
uint32_t shininess;

// factors set internally for more accuracy in deeper zooms:
double xStepLength;
double yStepLength;

float aNorm; // = scale.a / |(scale.a, scale.c)|
float bNorm;
float cNorm;
float dNorm;

static float3 getNormalVectorOf(float3 xVec, float3 yVec) {
    return fast_normalize(cross(xVec, yVec));
}

static float getPhongRefectionModelValue(float3 xVec, float3 yVec) {
    float3 normalVector = getNormalVectorOf(xVec, yVec);
    float cosineAlpha = max(0.f, dot(normalVector, lightVector));

    float3 reflectionDirection = 2 * cosineAlpha * normalVector - lightVector;
    float specularFactor = max(0.f, reflectionDirection.z); // viewer direction = {0,0,1}

    float brightness = ambientReflection + diffuseReflection * cosineAlpha;// + pown(specularFactor * specularReflection, shininess);

    return brightness;
}

static float getBrightness(double dzx, double dzy) {
    float3 xVec = (float3) { aNorm, cNorm, (float) (dzx / xStepLength) };
    float3 yVec = (float3) { bNorm, dNorm, (float) (dzy / yStepLength) };

    return getPhongRefectionModelValue(xVec, yVec);
}

static float4 adjustLight(float4 yuv, double dzx, double dzy) {
    float brightness = getBrightness(dzx, dzy);
    yuv.s0 = yuv.s0 * brightness;
    return yuv;
}

