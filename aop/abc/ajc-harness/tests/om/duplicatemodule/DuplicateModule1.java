module DuplicateModule {
    class A;
    aspect AspectA;
    __sig {
        pointcut call(* *(..));
    }
}