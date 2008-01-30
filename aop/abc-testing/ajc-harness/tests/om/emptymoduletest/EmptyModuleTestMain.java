import org.aspectj.testing.Tester;

/*
 * Created on Jun 1, 2005
 *
 */
/**
 * @author Neil Ongkingco
 *
 */
public class EmptyModuleTestMain {

    public static void main(String[] args) {
        A a = new A();
        a.f();
        
        //matches on the print and f
        Tester.checkEqual(AspectA.callCtr, 2, "AspectA.callCtr"); 
    }
}
