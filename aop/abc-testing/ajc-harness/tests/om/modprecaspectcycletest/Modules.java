module A {
    open B;
    friend A1;
    constrain C;
}

module B {
    friend B1;
}

module C {
    friend C1;
}