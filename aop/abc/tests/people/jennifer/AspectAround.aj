public aspect AspectAround {

    static int i = 8;
    pointcut test(): if (i == 6) && call(* *(..));

    pointcut test2(): call(* *(..));  
    Object around(): test2(){
        System.out.println("around before ");//+thisJoinPoint);
        Object o = proceed();
        System.out.println("around after");
        return o;
    }

}

