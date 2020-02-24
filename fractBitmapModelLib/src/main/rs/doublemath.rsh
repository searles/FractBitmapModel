static double __attribute__((overloadable)) abs(double a) {
    if(a < 0) return -a;
    return a;
}

static double __attribute__((overloadable)) exp(double d) {
    return exp((float) d);
}

static double __attribute__((overloadable)) log(double d) {
    return log((float) d);
}

static double __attribute__((overloadable)) sin(double d) {
    return sin((float) d);
}

static double __attribute__((overloadable)) cos(double d) {
    return cos((float) d);
}

static double __attribute__((overloadable)) sincos(double d, double* co) {
    float fco;
    float si = sincos((float) d, &fco);
    *co = (double) fco;
    return (double) si;
}


static double __attribute__((overloadable)) sinh(double d) {
    return sinh((float) d);
}

static double __attribute__((overloadable)) cosh(double d) {
    return cosh((float) d);
}

static double __attribute__((overloadable)) sqrt(double d) {
    return sqrt((float) d);
}

static double __attribute__((overloadable)) atan2(double y, double x) {
    return atan2((float) y, (float) x);
}

static double __attribute__((overloadable)) floor(double a) {
    return floor((float) a);
}

static int __attribute__((overloadable)) pow(int base, int exp) {
    int r = 1; if(exp < 0) { base = 1 / base; exp = -exp; }
    for(;exp; exp >>= 1, base *= base) if(exp & 1) r *= base;
    return r;
}

static double __attribute__((overloadable)) pow(double base, int exp) {
    double r = 1; if(exp < 0) { base = 1 / base; exp = -exp; }
    for(;exp; exp >>= 1, base *= base) if(exp & 1) r *= base;
    return r;
}

static double __attribute__((overloadable)) pow(double base, double ex) {
    return pow((float) base, (float) ex);
}

static double __attribute__((overloadable)) max(double a, double b) {
    if(a > b) return a;
    else return b;
}

static double __attribute__((overloadable)) min(double a, double b) {
    if(a > b) return a;
    else return b;
}

static double __attribute__((overloadable)) dot(double2 a, double2 b) {
    return a.x * b.x + a.y * b.y;
}
