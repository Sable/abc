/*
 * Created on Jun 21, 2005
 *
 */


/**
 * @author Neil Ongkingco
 *
 */
public aspect ArgsAspect {
    pointcut pc() : call(* f(..));
    public static int callCtr = 0;
    
    before() : pc() {
        callCtr++;
        System.out.println("before " + thisJoinPoint.getSignature());
    }
}
