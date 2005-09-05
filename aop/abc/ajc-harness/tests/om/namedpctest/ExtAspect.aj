aspect ExtAspect {
    public static int callCtr = 0;
    
    before() : call(* fib(..)) { 
        System.out.println("Before internalFib from ExtAspect");
    }
}