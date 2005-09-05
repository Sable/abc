/*
 * Created on Jul 27, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public aspect AspectB {
    before() : call(* A.f1(..)) {
        InheritPrecTestMain.addToCallOrder(2);
        System.out.println("AspectB");
    }
}
