module DuplicateModule {
    friend AspectB;
    expose : call(* *(..));
}