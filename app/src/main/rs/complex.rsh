static double2 csqr(double2 z) {
    return  (double2) { z.x * z.x - z.y * z.y, 2 * z.x * z.y};
}