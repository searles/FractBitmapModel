#include "complex.rsh"

static float3 createResult(int layer, double2 value, double height) {
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
	
	while(pc < codeSize) {
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
            case 5: (*((double2*) (&data[code[pc + 6]]))) = ((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}) + (*((double2*) (&data[code[pc + 5]]))); pc += 7; break; 
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
            case 11: (*((double2*) (&data[code[pc + 6]]))) = ((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}) - (*((double2*) (&data[code[pc + 5]]))); pc += 7; break; 
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
            case 16: (*((double2*) (&data[code[pc + 3]]))) = mul((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break; 
            // Mul: [Cplx, *Cplx]
            case 17: (*((double2*) (&data[code[pc + 6]]))) = mul(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break; 
            // === Div ===
            // Div: [*Real, *Real]
            case 18: (*((double*) (&data[code[pc + 3]]))) = (*((double*) (&data[code[pc + 1]]))) / (*((double*) (&data[code[pc + 2]]))); pc += 4; break; 
            // Div: [Real, *Real]
            case 19: (*((double*) (&data[code[pc + 4]]))) = (*((double*) (&code[pc + 1]))) / (*((double*) (&data[code[pc + 3]]))); pc += 5; break; 
            // Div: [*Cplx, *Cplx]
            case 20: (*((double2*) (&data[code[pc + 3]]))) = div((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break; 
            // Div: [Cplx, *Cplx]
            case 21: (*((double2*) (&data[code[pc + 6]]))) = div(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break; 
            // === Mod ===
            // Mod: [*Int, *Int]
            case 22: data[code[pc + 3]] = data[code[pc + 1]] % data[code[pc + 2]]; pc += 4; break; 
            // Mod: [Int, *Int]
            case 23: data[code[pc + 3]] = code[pc + 1] % data[code[pc + 2]]; pc += 4; break; 
            // Mod: [*Int, Int]
            case 24: data[code[pc + 3]] = data[code[pc + 1]] % code[pc + 2]; pc += 4; break; 
            // === Pow ===
            // Pow: [*Real, *Int]
            case 25: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), data[code[pc + 2]]); pc += 4; break; 
            // Pow: [Real, *Int]
            case 26: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&code[pc + 1]))), data[code[pc + 3]]); pc += 5; break; 
            // Pow: [*Real, Int]
            case 27: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), code[pc + 2]); pc += 4; break; 
            // Pow: [*Real, *Real]
            case 28: (*((double*) (&data[code[pc + 3]]))) = pow((*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break; 
            // Pow: [Real, *Real]
            case 29: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))); pc += 5; break; 
            // Pow: [*Real, Real]
            case 30: (*((double*) (&data[code[pc + 4]]))) = pow((*((double*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))); pc += 5; break; 
            // Pow: [*Cplx, *Int]
            case 31: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), data[code[pc + 2]]); pc += 4; break; 
            // Pow: [Cplx, *Int]
            case 32: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), data[code[pc + 5]]); pc += 7; break; 
            // Pow: [*Cplx, Int]
            case 33: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), code[pc + 2]); pc += 4; break; 
            // Pow: [*Cplx, *Real]
            case 34: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))); pc += 4; break; 
            // Pow: [Cplx, *Real]
            case 35: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double*) (&data[code[pc + 5]])))); pc += 7; break; 
            // Pow: [*Cplx, Real]
            case 36: (*((double2*) (&data[code[pc + 4]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))); pc += 5; break; 
            // Pow: [*Cplx, *Cplx]
            case 37: (*((double2*) (&data[code[pc + 3]]))) = pow((*((double2*) (&data[code[pc + 1]]))), (*((double2*) (&data[code[pc + 2]])))); pc += 4; break; 
            // Pow: [Cplx, *Cplx]
            case 38: (*((double2*) (&data[code[pc + 6]]))) = pow(((double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&code[pc + 3])))}), (*((double2*) (&data[code[pc + 5]])))); pc += 7; break; 
            // Pow: [*Cplx, Cplx]
            case 39: (*((double2*) (&data[code[pc + 6]]))) = pow((*((double2*) (&data[code[pc + 1]]))), ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))})); pc += 7; break; 
            // === Neg ===
            // Neg: [*Int]
            case 40: data[code[pc + 2]] = -data[code[pc + 1]]; pc += 3; break; 
            // Neg: [*Real]
            case 41: (*((double*) (&data[code[pc + 2]]))) = -(*((double*) (&data[code[pc + 1]]))); pc += 3; break; 
            // Neg: [*Cplx]
            case 42: (*((double2*) (&data[code[pc + 2]]))) = -(*((double2*) (&data[code[pc + 1]]))); pc += 3; break; 
            // === Recip ===
            // Recip: [*Real]
            case 43: (*((double*) (&data[code[pc + 2]]))) = 1.0 / (*((double*) (&data[code[pc + 1]]))); pc += 3; break; 
            // Recip: [*Cplx]
            case 44: (*((double2*) (&data[code[pc + 2]]))) = div((double2) {1., 0.}, (*((double2*) (&data[code[pc + 1]])))); pc += 3; break; 
            // === Abs ===
            // Abs: [*Int]
            case 45: data[code[pc + 2]] = abs(data[code[pc + 1]]); pc += 3; break; 
            // Abs: [*Real]
            case 46: (*((double*) (&data[code[pc + 2]]))) = abs((*((double*) (&data[code[pc + 1]])))); pc += 3; break; 
            // Abs: [*Cplx]
            case 47: (*((double*) (&data[code[pc + 2]]))) = abs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Assign ===
            // Assign: [*Int, *Int]
            case 48: data[code[pc + 1]] = data[code[pc + 2]]; pc += 3; break;
            // Assign: [*Int, Int]
            case 49: data[code[pc + 1]] = code[pc + 2]; pc += 3; break;
            // Assign: [*Real, *Real]
            case 50: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Real, Real]
            case 51: (*((double*) (&data[code[pc + 1]]))) = (*((double*) (&code[pc + 2]))); pc += 4; break;
            // Assign: [*Cplx, *Cplx]
            case 52: (*((double2*) (&data[code[pc + 1]]))) = (*((double2*) (&data[code[pc + 2]]))); pc += 3; break;
            // Assign: [*Cplx, Cplx]
            case 53: (*((double2*) (&data[code[pc + 1]]))) = ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}); pc += 6; break;
            // === Jump ===
            // Jump: [Int]
            case 54: pc = code[pc + 1]; break;
            // === Equal ===
            // Equal: [*Int, *Int]
            case 55: if(data[code[pc + 1]] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Equal: [Int, *Int]
            case 56: if(code[pc + 1] == data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === Less ===
            // Less: [*Int, *Int]
            case 57: if(data[code[pc + 1]] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Int, *Int]
            case 58: if(code[pc + 1] < data[code[pc + 2]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [*Int, Int]
            case 59: if(data[code[pc + 1]] < code[pc + 2]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [*Real, *Real]
            case 60: if((*((double*) (&data[code[pc + 1]]))) < (*((double*) (&data[code[pc + 2]])))) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Less: [Real, *Real]
            case 61: if((*((double*) (&code[pc + 1]))) < (*((double*) (&data[code[pc + 3]])))) pc = code[pc + 4]; else pc = code[pc + 5];break;
            // Less: [*Real, Real]
            case 62: if((*((double*) (&data[code[pc + 1]]))) < (*((double*) (&code[pc + 2])))) pc = code[pc + 4]; else pc = code[pc + 5];break;
            // === Next ===
            // Next: [*Int, *Int]
            case 63: if(++data[code[pc + 2]] < data[code[pc + 1]]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // Next: [Int, *Int]
            case 64: if(++data[code[pc + 2]] < code[pc + 1]) pc = code[pc + 3]; else pc = code[pc + 4];break;
            // === Sqrt ===
            // Sqrt: [*Real]
            case 65: (*((double*) (&data[code[pc + 2]]))) = sqrt((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sqrt: [*Cplx]
            case 66: (*((double2*) (&data[code[pc + 2]]))) = sqrt((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Exp ===
            // Exp: [*Real]
            case 67: (*((double*) (&data[code[pc + 2]]))) = exp((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Exp: [*Cplx]
            case 68: (*((double2*) (&data[code[pc + 2]]))) = exp((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Log ===
            // Log: [*Real]
            case 69: (*((double*) (&data[code[pc + 2]]))) = log((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Log: [*Cplx]
            case 70: (*((double2*) (&data[code[pc + 2]]))) = log((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sin ===
            // Sin: [*Real]
            case 71: (*((double*) (&data[code[pc + 2]]))) = sin((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sin: [*Cplx]
            case 72: (*((double2*) (&data[code[pc + 2]]))) = sin((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cos ===
            // Cos: [*Real]
            case 73: (*((double*) (&data[code[pc + 2]]))) = cos((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cos: [*Cplx]
            case 74: (*((double2*) (&data[code[pc + 2]]))) = cos((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Sinh ===
            // Sinh: [*Real]
            case 75: (*((double*) (&data[code[pc + 2]]))) = sinh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Sinh: [*Cplx]
            case 76: (*((double2*) (&data[code[pc + 2]]))) = sinh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cosh ===
            // Cosh: [*Real]
            case 77: (*((double*) (&data[code[pc + 2]]))) = cosh((*((double*) (&data[code[pc + 1]])))); pc += 3; break;
            // Cosh: [*Cplx]
            case 78: (*((double2*) (&data[code[pc + 2]]))) = cosh((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === ToReal ===
            // ToReal: [*Int]
            case 79: (*((double*) (&data[code[pc + 2]]))) = (double) data[code[pc + 1]]; pc += 3; break;
            // === Cons ===
            // Cons: [*Real, *Real]
            case 80: (*((double2*) (&data[code[pc + 3]]))) = (double2) {(*((double*) (&data[code[pc + 1]]))), (*((double*) (&data[code[pc + 2]])))}; pc += 4; break;
            // Cons: [Real, *Real]
            case 81: (*((double2*) (&data[code[pc + 4]]))) = (double2) {(*((double*) (&code[pc + 1]))), (*((double*) (&data[code[pc + 3]])))}; pc += 5; break;
            // Cons: [*Real, Real]
            case 82: (*((double2*) (&data[code[pc + 4]]))) = (double2) {(*((double*) (&data[code[pc + 1]]))), (*((double*) (&code[pc + 2])))}; pc += 5; break;
            // === Arc ===
            // Arc: [*Cplx]
            case 83: (*((double*) (&data[code[pc + 2]]))) = arc((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === RealPart ===
            // RealPart: [*Cplx]
            case 84: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).x; pc += 3; break;
            // === ImaginaryPart ===
            // ImaginaryPart: [*Cplx]
            case 85: (*((double*) (&data[code[pc + 2]]))) = (*((double2*) (&data[code[pc + 1]]))).y; pc += 3; break;
            // === Conj ===
            // Conj: [*Cplx]
            case 86: (*((double2*) (&data[code[pc + 2]]))) = conj((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Cabs ===
            // Cabs: [*Cplx]
            case 87: (*((double2*) (&data[code[pc + 2]]))) = cabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break;
            // === Rabs ===
            // Rabs: [*Cplx]
            case 88: (*((double2*) (&data[code[pc + 2]]))) = rabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break; 
            // === Iabs ===
            // Iabs: [*Cplx]
            case 89: (*((double2*) (&data[code[pc + 2]]))) = iabs((*((double2*) (&data[code[pc + 1]])))); pc += 3; break; 
            // === Point ===
            // Point: []
            case 90: (*((double2*) (&data[code[pc + 1]]))) = pt; pc += 2; break; 
            // === SetResult ===
            // SetResult: [*Int, *Cplx, *Real]
            case 91: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break; 
            // SetResult: [Int, *Cplx, *Real]
            case 92: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&data[code[pc + 3]])))); pc += 4; break; 
            // SetResult: [*Int, Cplx, *Real]
            case 93: result = createResult(data[code[pc + 1]], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]])))); pc += 7; break; 
            // SetResult: [Int, Cplx, *Real]
            case 94: result = createResult(code[pc + 1], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&data[code[pc + 6]])))); pc += 7; break; 
            // SetResult: [*Int, *Cplx, Real]
            case 95: result = createResult(data[code[pc + 1]], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break; 
            // SetResult: [Int, *Cplx, Real]
            case 96: result = createResult(code[pc + 1], (*((double2*) (&data[code[pc + 2]]))), (*((double*) (&code[pc + 3])))); pc += 5; break; 
            // SetResult: [*Int, Cplx, Real]
            case 97: result = createResult(data[code[pc + 1]], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6])))); pc += 8; break; 
            // SetResult: [Int, Cplx, Real]
            case 98: result = createResult(code[pc + 1], ((double2) {(*((double*) (&code[pc + 2]))), (*((double*) (&code[pc + 4])))}), (*((double*) (&code[pc + 6])))); pc += 8; break; 
		}
	}
	
	return result;
}