module Module {
    class A;
    open ModuleB;
    advertise : call(* f1(..));
}

module ModuleB {
    class B;
    constrain ModuleC;
}

module ModuleC {
    class C;
    advertise : call(* f3(..));
}