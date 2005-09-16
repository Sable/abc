aspect ExtAspectA {
    pointcut f1(): call(* A.f1(..));
    pointcut f2(): call(* A.f2(..));
    pointcut f3(): call(* A.f3(..));
    pointcut f4(): call(* A.f4(..));
    
    before() : f1() || f2() || f3() || f4() {
        System.out.println("ExtAspectA " + thisJoinPoint.getSignature());
    }
}