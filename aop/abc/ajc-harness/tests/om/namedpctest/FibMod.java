module FibMod {
    class A;
    aspect AspectA;
    __sig {
        pointcut AspectA.internalFib();
        method * A.fib(..);
    }
}