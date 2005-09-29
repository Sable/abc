module Module {
    class A;
    expose : call(* f(..)) && (args(int) || args(A));
}