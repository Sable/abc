/*
 * Created on Jul 27, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public aspect AspectA {
    before() : call(* A.f1(..)) {
        InheritPrecTestMain.addToCallOrder(1);
        System.out.println("AspectA");
    }
}
