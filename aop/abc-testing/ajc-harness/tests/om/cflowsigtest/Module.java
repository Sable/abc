module Module {
    class A;
    expose : cflow(call (* A.a()));
    expose : cflow(call (* A.d()));
}