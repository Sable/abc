//same as module 1, except Fib is applied before ACache
module Module2 {
    class A;
    aspect ACache;
    aspect Fib;
    __sig {
        method int A.fib(int);
    }
}