module ModuleC {
    class C;
    friend AspectC;
    advertise : call(* f2(..));
    advertise : call(* f3(..));
}