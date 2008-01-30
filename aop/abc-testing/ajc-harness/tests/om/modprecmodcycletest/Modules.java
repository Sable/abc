module A {
    constrain B;
    friend A1, A2;
    open C;
}

module B {
    friend B1, B2;
}

module C {
    friend C1, C2;
}

module X {
    friend X1, X2;
}