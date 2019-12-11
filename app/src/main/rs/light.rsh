// parameters:
float3 lightVector;
float ambientLight;
float diffuseLight;

float specularStrength;
uint32_t shininess;
float3 viewerVector;


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
    float cosineAlpha = dot(normalVector, lightVector);

    float3 reflectionDirection = 2 * cosineAlpha * normalVector - lightVector;
    float specularFactor = max(0.f, dot(reflectionDirection, viewerVector));

    float brightness = ambientLight + diffuseLight * (cosineAlpha * cosineAlpha + 2.f * cosineAlpha + 1.f) / 4.f + pown(specularFactor * specularStrength, shininess);

    return brightness;
}

static float getBrightness(double dzx, double dzy) {
    float3 xVec = (float3) { aNorm, cNorm, (float) (dzx / xStepLength) };
    float3 yVec = (float3) { bNorm, dNorm, (float) (dzy / yStepLength) };

    return getPhongRefectionModelValue(xVec, yVec);
}

