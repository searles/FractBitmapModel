static double2 csqr(double2 z) {
    return  (double2) { z.x * z.x - z.y * z.y, 2 * z.x * z.y};
}

static double2 cmul(double2 a, double2 b) {
    return (double2){ a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x };
}

static double2 cdiv(double2 a, double2 b) {
    double r = b.x * b.x + b.y * b.y;
    return (double2){ (a.x * b.x + a.y * b.y) / r, (-a.x * b.y + a.y * b.x) / r };
}

static double2 crecip(double2 a) {
    double r = a.x * a.x + a.y * a.y;
    return (double2){ a.x / r, -a.y / r };
}

static double2 cabs(double2 a) {
    double2 b = a;
    if(b.x < 0) b.x = -b.x;
    if(b.y < 0) b.y = -b.y;
    return b;
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

static double2 __attribute__((overloadable)) pow(double2 base, int exp) {
    double2 r = (double2){1., 0.};
    if(exp < 0) { base = crecip(base); exp = -exp; }
    for(;exp; exp >>= 1, base = cmul(base, base))
        if(exp & 1) r = cmul(r, base);
    return r;
}

// -----

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

static double __attribute__((overloadable)) sinh(double d) {
    return sinh((float) d);
}

static double __attribute__((overloadable)) cosh(double d) {
    return cosh((float) d);
}

static double __attribute__((overloadable)) sqrt(double d) {
    return sqrt((float) d);
}

static double __attribute__((overloadable)) rad(double2 f) {
	return sqrt(f.x * f.x + f.y * f.y);
}

static double __attribute__((overloadable)) arc(double2 f) {
    return atan2((float) f.y, (float) f.x);
}

static double __attribute__((overloadable)) pow(double base, double ex) {
    return pow((float) base, (float) ex);
}


// -----

static double2 __attribute__((overloadable)) log(double2 f) {
	return (double2) { log(rad(f)), arc(f) };
}

static double2 __attribute__((overloadable)) exp(double2 f) {
	double e = exp(f.x);
	double c = cos(f.y);
	double s = sin(f.y);
	return (double2) { e * c, e * s };
}

static double2 __attribute__((overloadable)) pow(double2 base, double power) {
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

    // FIXME
	// base^power = exp(log base * power) =
	// = exp ((log rad base + i arc base) * power)
	// = exp (power * log rad base + i power arc base)
	// = rad base ^ power * exp(i power arc base)
	// = rad base ^ power * (cos power arc base + i sin power arc base)
	double r = pow(rad(base), power);
	double pa = power * arc(base);

	double c = cos(pa); double s = sin(pa);

	return (double2) {r * c, r * s};
}

static double2 __attribute__((overloadable)) pow(double2 base, double2 power) {
    // FIXME
	// base^power = exp(log base * power) =
	// = exp ((log rad base + i ab) * power)
	// = exp ((log rb + i arc base) * power)
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

	double lrb = log(rad(base));
	double ab = arc(base);

	// = exp (lrb * pr - ab * pi, lrb * pi + ab * pr)
	double2 prod = (double2) {lrb * power.x - ab * power.y, lrb * power.y + ab * power.x};

	return exp(prod);
}

static double2 __attribute__((overloadable)) sin(double2 a) {
	double2 eia = exp((double2) {-a.y, a.x});
	double2 eia2 = eia * eia;

    eia2 = (double2) { -eia2.y, eia2.x - 1 };
    eia = (double2) { 2 * eia.x, 2 * eia.y };

	return cdiv(eia2, eia);
}

static double2 __attribute__((overloadable)) cos(double2 a) {
	// cos x = (e^iz - e^-iz) / 2i = (e^2iz + 1) / (2i e^iz)
	double2 eia = exp((double2) {-a.y, a.x});
	double2 eia2 = eia * eia;

    eia2.x += 1;
    eia = (double2) { 2 * eia.x, 2 * eia.y };

	return cdiv(eia2, eia);
}

static double2 __attribute__((overloadable)) sinh(double2 a) {
	double2 eia = exp((double2) {a.x, a.y});
	double2 eia2 = eia * eia;

    eia2 = (double2) { -eia2.y, eia2.x - 1 };
    eia = (double2) { 2 * eia.x, 2 * eia.y };

	return cdiv(eia2, eia);
}

static double2 __attribute__((overloadable)) cosh(double2 a) {
	double2 eia = exp((double2) {a.x, a.y});
	double2 eia2 = eia * eia;

    eia2.x += 1;
    eia = (double2) { 2 * eia.x, 2 * eia.y };

	return cdiv(eia2, eia);
}

static double2 __attribute__((overloadable)) sqrt(double2 f) {
	double r = rad(f);
	double2 ret = { sqrt((r + f.x) / 2), sqrt((r - f.x) / 2) };
	if(f.y < 0) ret.y = -ret.y;
	return ret;
}

