aspect Fib {
    public static int callCtr = 0;
    pointcut fib(int x) : call(int A.fib(int)) && args(x);
    
    int around(int x, A a) : fib(x) && target(a){
        callCtr++;
        System.out.println("around " + thisJoinPoint.getSignature() + "in Fib");
        if (x < 3) { return proceed(x,a); }
        else { return a.fib(x-1) + a.fib(x-2); }
    }
}