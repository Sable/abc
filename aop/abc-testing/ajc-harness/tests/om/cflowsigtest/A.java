/*
 * Created on Jun 2, 2005
 *
 */

/**
 * @author Neil Ongkingco
 *
 */
public class A {
    void a() {
        System.out.println("a");
        b();
        e();
    }
    
    void b() {
        System.out.println("b");
    }
    
    void c() {
        System.out.println("c");
    }
    
    void d() {
        System.out.println("d");
        e();
    }
    
    void e() {
        System.out.println("e");
    }
}
