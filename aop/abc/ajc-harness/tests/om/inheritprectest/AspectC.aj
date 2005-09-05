/*
 * Created on Jul 27, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public aspect AspectC {
    before() : call(* A.f1(..)) {
        InheritPrecTestMain.addToCallOrder(3);
        System.out.println("AspectC");
    }
}
