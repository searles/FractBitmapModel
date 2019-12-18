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

static float4 getPhongRefectionModelValue(float3 normalVector, float4 color) {
    float cosineAlpha = max(0.f, dot(normalVector, lightVector));
    float diffuse = diffuseReflection * cosineAlpha;

    float3 reflectionDirection = 2 * cosineAlpha * normalVector - lightVector;
    float specularBase = max(0.f, reflectionDirection.z); // viewer direction = {0,0,1}
    float specular = pown(specularBase * specularReflection, shininess); // since the light is white, use it as white.

    color.s0 = color.s0 * ambientReflection + diffuse + specular;

    return color;
}

static float3 getNormalVectorOf(float3 xVec, float3 yVec) {
    return fast_normalize(cross(xVec, yVec));
}

static float4 adjustLight(float4 color, double dzx, double dzy) {
    float3 xVec = (float3) { aNorm, cNorm, (float) (dzx / xStepLength) };
    float3 yVec = (float3) { bNorm, dNorm, (float) (dzy / yStepLength) };

    float3 normalVector = getNormalVectorOf(xVec, yVec);

    return getPhongRefectionModelValue(normalVector, color);
}

