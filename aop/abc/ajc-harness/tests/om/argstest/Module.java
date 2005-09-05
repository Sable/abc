module Module {
    class A;
    __sig {
        pointcut call(* f(..)) && (args(int) || args(A));
    }
}