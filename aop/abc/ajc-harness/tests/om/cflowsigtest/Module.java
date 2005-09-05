module Module {
    class A;
    __sig {
        pointcut cflow(call (* A.a()));
        pointcut cflow(call (* A.d()));
    }
}