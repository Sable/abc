/*
 * Created on Jun 1, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public aspect AspectA {
    public static int callCtr = 0;
    before() : call(* f*(..)) && !within(AspectA) {
        callCtr++;
        System.out.println("Before " + thisJoinPoint.getSignature());
    }
}
