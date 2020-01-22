#include "doublemath.rsh"

static double2 __attribute__((overloadable)) mul(double2 a, double2 b) {
    return (double2){ a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x };
}

static double2 __attribute__((overloadable)) div(double2 a, double2 b) {
    double r = b.x * b.x + b.y * b.y;
    return (double2){ (a.x * b.x + a.y * b.y) / r, (-a.x * b.y + a.y * b.x) / r };
}

static double2 __attribute__((overloadable)) rec(double2 a) {
    double r = a.x * a.x + a.y * a.y;
    return (double2){ a.x / r, -a.y / r };
}

static double __attribute__((overloadable)) abs(double2 z) {
	// avoid overflow/underflow
	double a = abs(z.x);
	double b = abs(z.y);

	// argument for sqrt is in interval 1..2
	if(a > b) {
		double quot = b / a;
		// TODO fast sqrt
		return a * sqrt(1 + quot * quot);
	} else if(b < a) {
		double quot = a / b;
		return b * sqrt(1 + quot * quot);
	} else {
	    return a * 1.41421356237;
	}
}

static double __attribute__((overloadable)) arc(double2 f) {
    return atan2(f.y, f.x);
}

static double2 __attribute__((overloadable)) exp(double2 z) {
	double ez = exp(z.x);

	double si; // sin(z.y);
	double co; // cos(z.y);
	si = sincos(z.y, &co);

	double2 result = ez * (double2) {co, si};

	return result;
}

static double2 __attribute__((overloadable)) log(double2 z) {
	return (double2) { log(abs(z)), arc(z) };
}

static double2 __attribute__((overloadable)) pow(double2 base, int exp) {
    double2 r = (double2){1., 0.};
    if(exp < 0) { base = rec(base); exp = -exp; }
    for(;exp; exp >>= 1, base = mul(base, base))
        if(exp & 1) r = mul(r, base);
    return r;
}

static double2 __attribute__((overloadable)) pow(double2 base, double power) {
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

	double r = pow(abs(base), power);
	double pa = power * arc(base);

    double si, co;
    si = sincos(pa, &co);

	return (double2) {r * co, r * si};
}

static double2 __attribute__((overloadable)) pow(double2 base, double2 power) {
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

	double lrb = log(abs(base));
	double ab = arc(base);

	// = exp (lrb * pr - ab * pi, lrb * pi + ab * pr)
	double2 prod = (double2) {lrb * power.x - ab * power.y, lrb * power.y + ab * power.x};

	return exp(prod);
}

static double2 cabs(double2 a) {
    if(a.x < 0) a.x = -a.x;
    if(a.y < 0) a.y = -a.y;
    return a;
}

static double2 __attribute__((overloadable)) sqrt(double2 f) {
	double r = abs(f);
	double2 ret = { sqrt((r + f.x) / 2), sqrt((r - f.x) / 2) };
	if(f.y < 0) ret.y = -ret.y;
	return ret;
}

static double2 __attribute__((overloadable)) conj(double2 f) {
	return (double2) {f.x, -f.y};
}

static double2 __attribute__((overloadable)) muli(double2 f) {
	return (double2) {-f.y, f.x};
}

static double2 __attribute__((overloadable)) rabs(double2 f) {
	return (double2) {abs(f.x), f.y};
}

static double2 __attribute__((overloadable)) iabs(double2 f) {
	return (double2) {f.x, abs(f.y)};
}

static double2 __attribute__((overloadable)) sinh(double2 z) {
	double2 ez = exp(z);
	return (ez - rec(ez)) / 2;
}

static double2 __attribute__((overloadable)) cosh(double2 z) {
	double2 ez = exp(z);
	return (ez + rec(ez)) / 2;
}


static double2 __attribute__((overloadable)) sin(double2 z) {
	return -muli(sinh(muli(z)));
}

static double2 __attribute__((overloadable)) cos(double2 z) {
	return cosh(muli(z));
}


