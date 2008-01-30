/*
 * Created on Jun 1, 2005
 *
 */
/**
 * @author Neil Ongkingco
 *
 */
public aspect ExtAspectA {
    public static int callCtr = 0;
    
    pointcut pc() : call(* A.*(..)) || call(* B.*(..)) || call(* C.*(..));
    
    before() : pc() {
        System.out.println("Before " + thisJoinPoint.getSignature());
        callCtr++;
    }
}
