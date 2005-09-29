module DuplicateModule {
    class A;
    friend AspectA;
    expose : call(* *(..));
}