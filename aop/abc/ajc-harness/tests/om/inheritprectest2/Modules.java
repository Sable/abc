module A {
    open B;
    friend A1, A2, A3;
    constrain C;
}
module B {
    friend B1, B2, B3;
}
module C {
    friend C1, C2, C3;
}