//Example taken from Aldrich's paper. Aspect precedence is 
//"last declared goes first", so ACache has a higher precedence than Fib
module Module1 {
    class A;
    aspect Fib;
    aspect ACache;
    
    //Signatures. Temporarily using __sig keyword (instead of just sig) since some
    //of the ajc test cases have variables named sig
    __sig {
        method int A.fib(int);
    }
}