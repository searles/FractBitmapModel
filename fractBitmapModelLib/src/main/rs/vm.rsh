/*
 * TODO the vm.
 */
static float3 valueAt(double2 pt) {

    int32_t maxIterationCount = 1000;
    float bailoutValue = 16.f;

    float logBailoutValue = native_log(bailoutValue);

    uint32_t i = 0;
    double2 z = (double2) {0.0, 0.0};

    while(i < maxIterationCount) {
        z = csqr(z) + pt;
        float d = fast_length(convert_float2(z));

        if(d > bailoutValue) {
            float smooth = native_log2(native_log(d) / logBailoutValue);
            float value = native_log(i - smooth);
            value = value - floor(value);
            return (float3) {value, 0.f, value};
        }

        i++;
    }
    return (float3) {1.f, 0.f, 0.f};
}

