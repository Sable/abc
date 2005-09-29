module Module {
    class A;
    advertise : call(* f1(..));
    advertise to ExtAspectA : call(* f2(..));
    expose: call(* f3(..));
    expose to ExtAspectB : call(* f4(..));
}