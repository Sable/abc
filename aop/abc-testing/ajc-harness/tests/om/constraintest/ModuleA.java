module ModuleA {
    class A;
    friend AspectA;
    open ModuleB;
    advertise : call(* f1(..));
}