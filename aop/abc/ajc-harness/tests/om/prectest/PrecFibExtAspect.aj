aspect PrecFibExtAspect {
    
    declare precedence : PrecFibExtAspect, Fib;
	declare precedence : PrecFibExtAspect, ACache;

    public static int callCtr = 0;
    
    pointcut fib(int x): call(* fib(int)) && args(x);
    
    before(int x) : fib(x) {
        System.out.println("before " + thisJoinPoint.getSignature() + " in FibExtAspect");
        callCtr++;
    }
}