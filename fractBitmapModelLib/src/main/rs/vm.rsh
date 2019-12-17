#include "complex.rsh"

static float3 createResult(int layer, double2 value, double height) {
    //rsDebug("setting layer", layer);
    //rsDebug("setting value", value);
    //rsDebug("setting height", height);
    float2 fValue = convert_float2(value);

    fValue = fValue - floor(fValue);

    return (float3) {
        (float) (fValue.x + layer),
        (float) fValue.y,
        (float) height
    };
}

int *code;
uint32_t codeSize;

static float3 valueAt(double2 pt) {
	uint32_t pc = 0;
	
	int data[256];
	
	float3 result;

	//rsDebug("codeSize=", codeSize);

    for(int i = 0; i < codeSize; ++i) {
        //rsDebug("code[]", code[i]);
    }

	while(pc < codeSize) {
	    //rsDebug("pc=", pc);
	    //rsDebug("op=", code[pc]);
		switch(code[pc]) {
            // === Add ===
            // Add: [*Int, *Int]
            case 0: data[code[pc + 3]] = data[code[pc + 1]] + data[code[pc + 2]]; pc += 4; break;
            // Add: [Int, *Int]
            case 1: data[code[pc + 3]] = code[pc + 1] + data[code[pc + 2]]; pc += 4; break;
            // Add: [*Real, *Real]
            case 2: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) + (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Add: [Real, *Real]
            case 3: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) + (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Add: [*Cplx, *Cplx]
            case 4: (*((double2*) (&data[code[pc + 3]]))) = (*((double2*) (&data[code[pc + 1]]))) + (*((double2*) (&data[code[pc + 2]]))); pc += 4; break;
            // Add: [Cplx, *Cplx]
            case 5: (*((double2*) (&data[code[pc + 6]]))) = (*((double2*) (&code[pc + 1]))) + (*((double2*) (&data[code[pc + 5]]))); pc += 7; break;
            // === Sub ===
            // Sub: [*Int, *Int]
            case 6: data[code[pc + 3]] = data[code[pc + 1]] - data[code[pc + 2]]; pc += 4; break;
            // Sub: [Int, *Int]
            case 7: data[code[pc + 3]] = code[pc + 1] - data[code[pc + 2]]; pc += 4; break;
            // Sub: [*Real, *Real]
            case 8: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) - (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Sub: [Real, *Real]
            case 9: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) - (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Sub: [*Cplx, *Cplx]
            case 10: (*((double2*) (&data[code[pc + 3]]))) = (*((double2*) (&data[code[pc + 1]]))) - (*((double2*) (&data[code[pc + 2]]))); pc += 4; break;
            // Sub: [Cplx, *Cplx]
            case 11: (*((double2*) (&data[code[pc + 6]]))) = (*((double2*) (&code[pc + 1]))) - (*((double2*) (&data[code[pc + 5]]))); pc += 7; break;
            // === Mul ===
            // Mul: [*Int, *Int]
            case 12: data[code[pc + 3]] = data[code[pc + 1]] * data[code[pc + 2]]; pc += 4; break;
            // Mul: [Int, *Int]
            case 13: data[code[pc + 3]] = code[pc + 1] * data[code[pc + 2]]; pc += 4; break;
            // Mul: [*Real, *Real]
            case 14: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) * (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Mul: [Real, *Real]
            case 15: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) * (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Mul: [*Cplx, *Cplx]
            case 16: (*((double2*) (&data[code[pc + 3]]))) = cmul((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Mul: [Cplx, *Cplx]
            case 17: (*((double2*) (&data[code[pc + 6]]))) = cmul((*((double2*) (&code[pc + 1]))), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Div ===
            // Div: [*Real, *Real]
            case 18: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) / (*((double*) (&data[code[pc + 2]]))); pc += 4; break;
            // Div: [Real, *Real]
            case 19: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) / (*((double*) (&data[code[pc + 3]]))); pc += 5; break;
            // Div: [*Cplx, *Cplx]
            case 20: (*((double2*) (&data[code[pc + 3]]))) = cdiv((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Div: [Cplx, *Cplx]
            case 21: (*((double2*) (&data[code[pc + 6]]))) = cdiv((*((double2*) (&code[pc + 1]))), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Mod ===
            // Mod: [Int, *Int]
            case 22: data[code[pc + 3]] = code[pc + 1] % data[code[pc + 2]]; pc += 4; break;
            // Mod: [*Int, Int]
            case 23: data[code[pc + 3]] = data[code[pc + 1]] % code[pc + 2]; pc += 4; break;
            // Mod: [*Int, *Int]
            case 24: data[code[pc + 3]] = data[code[pc + 1]] % data[code[pc + 2]]; pc += 4; break;
            // === Pow ===
            // Pow: [*Real, *Real]
            case 25: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Real, *Real]
            case 26: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))); pc += 5; break;
            // Pow: [*Cplx, *Int]
            case 27: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), data[code[pc + 2]]); pc += 4; break;
            // Pow: [Cplx, *Int]
            case 28: (*((double2*) (&data[code[pc + 6]]))) = pow((*((double2*) (&code[pc + 1]))), data[code[pc + 5]]); pc += 7; break;
            // Pow: [*Cplx, *Real]
            case 29: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Cplx, *Real]
            case 30: (*((double2*) (&data[code[pc + 6]]))) = pow((*((double2*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 5]])))); pc += 7; break;
            // Pow: [*Cplx, *Cplx]
            case 31: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break;
            // Pow: [Cplx, *Cplx]
            case 32: (*((double2*) (&data[code[pc + 6]]))) = pow((*((double2*) (&code[pc + 1]))), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break;
            // === Neg ===
            // Neg: [*Int]
            case 33: data[code[pc + 2]] = -data[code[pc + 1]]; pc += 3; break;
            // Neg: [*Real]
            case 34: (*((double*) (&data[code[pc + 2]]))) = -(*((double*) (&data[code[pc + 1]]))); pc += 3; break;
            // Neg: [*Cplx]
            case 35: (*((double2*) (&data[code[pc + 2]]))) = -(*((double2*) (&data[code[pc + 1]]))); pc += 3; break;
            // === Reciprocal ===
            // Reciprocal: [*Real]
            case 36: (*((double*) (&data[code[pc + 2]]))) = 1.0 / (*((double*) (&data[code[pc + 1]]))); pc += 3; break;
            // Reciprocal: [*Cplx]
            case 37: (*((double2*) (&data[code[pc + 2]]))) = cdiv((double2) {1., 0.}, (*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Abs ===
            // Abs: [*Int]
            case 38: if(data[code[pc + 1]] < 0) data[code[pc + 2]] = -data[code[pc + 1]]; else data[code[pc + 2]] = data[code[pc + 1]]; pc += 3; break;
            // Abs: [*Real]
            case 39: if((*((double*) (&data[code[pc + 1]]))) < 0) (*((double*) (&data[code[pc + 2]]))) = -(*((double*) (&data[code[pc + 1]]))); else (*((double*) (&data[code[pc + 2]]))) = (*((double*) (&data[code[pc + 1]]))); pc += 3; break;
            // Abs: [*Cplx]
            case 40: (*((double2*) (&data[code[pc + 2]]))) = cabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Assign ===
            // Assign: [*Int, *Int]
            case 41: data[code[pc + 1]] = data[code[pc + 2]]; pc += 3; break;
            // Assign: [*Int, Int]
            case 42: data[code[pc + 1]] = code[pc + 2]; pc += 3; break;
            // Assign: [*Real, *Real]
            case 43: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Real, Real]
            case 44: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&code[pc + 2]))); pc += 4; break;
            // Assign: [*Cplx, *Cplx]
            case 45: (*((double2*) (&data[code[pc + 1]]))) = (*((double2*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Cplx, Cplx]
            case 46: (*((double2*) (&data[code[pc + 1]]))) = (*((double2*) (&code[pc + 2]))); pc += 6; break;
            // === Jump ===
            // Jump: [Int]
            case 47: pc = code[pc + 1]; break;
            // === Equal ===
            // Equal: [*Int, *Int]
            case 48: if(data[code[pc + 1]] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Equal: [Int, *Int]
            case 49: if(code[pc + 1] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === Less ===
            // Less: [*Int, *Int]
            case 50: if(data[code[pc + 1]] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Int, *Int]
            case 51: if(code[pc + 1] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [*Real, *Real]
            case 52: if((*((double*) (&data[code[pc + 1]]))) < (*((double*) (&data[code[pc + 2]])))) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Real, *Real]
            case 53: if((*((double*) (&code[pc + 1]))) < (*((double*) (&data[code[pc + 3]])))) pc = code[pc + 4]; else pc = code[pc + 5];break;
            // === Next ===
            // Next: [*Int, Int]
            case 54: if(++data[code[pc + 1]] < code[pc + 2]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Next: [*Int, *Int]
            case 55: if(++data[code[pc + 1]] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === RealPart ===
            // RealPart: [*Cplx]
            case 56: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).x; pc += 3; break;
            // === ImaginaryPart ===
            // ImaginaryPart: [*Cplx]
            case 57: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).y; pc += 3; break;
            // === Point ===
            // Point: []
            case 58: (*((double2*) (&data[code[pc + 1]]))) = pt; pc += 2; break;
            // === SetResult ===
            // SetResult: [*Int, *Cplx, *Real]
            case 59: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break;
            // SetResult: [Int, *Cplx, *Real]
            case 60: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break;
            // SetResult: [*Int, Cplx, *Real]
            case 61: result = createResult(data[code[pc + 1]], (*((double2*) (&code[pc + 2]))), (*((double*) (&data[code[pc + 6]])))); pc += 7; break;
            // SetResult: [Int, Cplx, *Real]
            case 62: result = createResult(code[pc + 1], (*((double2*) (&code[pc + 2]))), (*((double*) (&data[code[pc + 6]])))); pc += 7; break;
            // SetResult: [*Int, *Cplx, Real]
            case 63: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break;
            // SetResult: [Int, *Cplx, Real]
            case 64: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break;
            // SetResult: [*Int, Cplx, Real]
            case 65: result = createResult(data[code[pc + 1]], (*((double2*) (&code[pc + 2]))), (*((double*) (&code[pc + 6])))); pc += 8; break;
            // SetResult: [Int, Cplx, Real]
            case 66: result = createResult(code[pc + 1], (*((double2*) (&code[pc + 2]))), (*((double*) (&code[pc + 6])))); pc += 8; break;
            // === Sqrt ===
            // Sqrt: [*Real]
            case 67: (*((double*) (&data[code[pc + 2]]))) = sqrt((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sqrt: [*Cplx]
            case 68: (*((double2*) (&data[code[pc + 2]]))) = sqrt((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Exp ===
            // Exp: [*Real]
            case 69: (*((double*) (&data[code[pc + 2]]))) = exp((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Exp: [*Cplx]
            case 70: (*((double2*) (&data[code[pc + 2]]))) = exp((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Log ===
            // Log: [*Real]
            case 71: (*((double*) (&data[code[pc + 2]]))) = log((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Log: [*Cplx]
            case 72: (*((double2*) (&data[code[pc + 2]]))) = log((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sin ===
            // Sin: [*Real]
            case 73: (*((double*) (&data[code[pc + 2]]))) = sin((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sin: [*Cplx]
            case 74: (*((double2*) (&data[code[pc + 2]]))) = sin((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cos ===
            // Cos: [*Real]
            case 75: (*((double*) (&data[code[pc + 2]]))) = cos((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cos: [*Cplx]
            case 76: (*((double2*) (&data[code[pc + 2]]))) = cos((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sinh ===
            // Sinh: [*Real]
            case 77: (*((double*) (&data[code[pc + 2]]))) = sinh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sinh: [*Cplx]
            case 78: (*((double2*) (&data[code[pc + 2]]))) = sinh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cosh ===
            // Cosh: [*Real]
            case 79: (*((double*) (&data[code[pc + 2]]))) = cosh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cosh: [*Cplx]
            case 80: (*((double2*) (&data[code[pc + 2]]))) = cosh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Rad ===
            // Rad: [*Cplx]
            case 81: (*((double*) (&data[code[pc + 2]]))) = rad((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Arc ===
            // Arc: [*Cplx]
            case 82: (*((double*) (&data[code[pc + 2]]))) = arc((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
		}
	}

	//rsDebug("result=", result);
	
	return result;
}