/*
 * Created on Jun 1, 2005
 *
 */
import org.aspectj.testing.Tester;
/**
 * @author Neil Ongkingco
 *
 */
public class InheritSigTest {
    public static void main(String args[]) {
        A a = new A();
        B b = new B();
        C c = new C();
        
        a.f1();
        a.f2();
        a.f3();
        
        b.f1();
        b.f2();
        b.f3();
        
        c.f1();
        c.f2();
        c.f3();
        
        Tester.checkEqual(ExtAspectA.callCtr, 5, "ExtAspect.callCtr");
    }
}
