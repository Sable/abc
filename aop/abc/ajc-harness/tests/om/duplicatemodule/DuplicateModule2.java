module DuplicateModule {
    aspect AspectB;
    __sig {
        pointcut call(* *(..));
    }
}