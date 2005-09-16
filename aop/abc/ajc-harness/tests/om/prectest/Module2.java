//same as module 1, except Fib is applied before ACache
//note: This is also a test for aspect_list, as the friend statement has 2 aspects
module Module2 {
    class A;
    friend ACache, Fib;
    
    advertise() : call(int A.fib(int));
}