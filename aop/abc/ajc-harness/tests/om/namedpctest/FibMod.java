module FibMod {
    class A;
    friend AspectA;
    
    expose : AspectA.internalFib();
    advertise : call(* A.fib(..));
}