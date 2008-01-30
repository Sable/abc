/*
 * Created on Jun 21, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public class A {
    public void f(int i) {
        System.out.println("f(int)");
    }
    
    public void f(float i) {
        System.out.println("f(float)");
    }
    
    public void f(A i) {
        System.out.println("f(A)");
    }
}
