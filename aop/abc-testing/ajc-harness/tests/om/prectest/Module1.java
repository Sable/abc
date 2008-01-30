//Example taken from Aldrich's paper. Aspect precedence is 
//"last declared goes first", so ACache has a higher precedence than Fib
//note: This is also a test for aspect_list, as the friend statement has 2 aspects
module Module1 {
    class A;
    class Fib || ACache;

    friend ACache, Fib;
    
    advertise : call(int A.fib(int));
}