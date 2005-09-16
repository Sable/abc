aspect ExtAspectB {
    pointcut f1() : ExtAspectA.f1();
    pointcut f2() : ExtAspectA.f2();
    pointcut f3() : ExtAspectA.f3();
    pointcut f4() : ExtAspectA.f4();
    
    before() : f1() || f2() || f3() || f4() {
        System.out.println("ExtAspectB " + thisJoinPoint.getSignature());
    }
}