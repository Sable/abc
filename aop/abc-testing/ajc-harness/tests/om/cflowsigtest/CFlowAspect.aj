/*
 * Created on Jun 2, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public aspect CFlowAspect {
    pointcut pc() : call(* b(..)) && !within(CFlowAspect);
    pointcut pc2() : call(* e(..)) && !within(CFlowAspect);
    
    public static int callCtrA = 0;
    public static int callCtrE = 0;
    
    before() : pc() {
        callCtrA++;
        System.out.println("Before "  + thisJoinPoint.getSignature());
    }
    
    before() : pc2() {
        callCtrE++;
        System.out.println("Before "  + thisJoinPoint.getSignature());
    }
}
