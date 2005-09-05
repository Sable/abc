aspect AspectA {
    public static int callCtr = 0;
    pointcut internalFib() : call(* fib(..)) && within(A);
    
    before() : internalFib() {
        callCtr++;
        System.out.println("Before internalFib from AspectA");
    }
}