module ModuleB {
    class B;
    friend AspectB;
    constrain ModuleC;
    advertise : call(* f2(..));
}