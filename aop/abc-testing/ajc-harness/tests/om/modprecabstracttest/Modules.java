module A {
    open B;
    friend A1;
    open C;
}
module B {
    friend B1;
}
module C {
    friend C1;
}
module X {
    open Y;
    friend X1;
    constrain Z;
}
module Y {
    friend Y1;
}
module Z {
}