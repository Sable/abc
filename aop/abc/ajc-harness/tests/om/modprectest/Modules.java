module A {
    open B;
    friend A1,A2,A3;
    open C;
}
module B {
    friend B1,B2,B3;
}
module C {
    friend C1,C2,C3;
}
module X {
    open Y;
    friend X1, X2, X3;
    constrain Z;
}
module Y {
    friend Y1, Y2, Y3;
}
module Z {
}