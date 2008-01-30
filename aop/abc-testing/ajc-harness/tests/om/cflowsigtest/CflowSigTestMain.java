/*
 * Created on Jun 2, 2005
 *
 */
import org.aspectj.testing.Tester;

/**
 * @author Neil Ongkingco
 *
 */
public class CflowSigTestMain {
    public static void main(String args[]) {
        A a = new A();
        a.a();
        a.b();
        a.c();
        a.d();
        a.e();
        
        Tester.checkEqual(CFlowAspect.callCtrA, 1, "CFlowAspect.callCtrA");
        Tester.checkEqual(CFlowAspect.callCtrE, 2, "CFlowAspect.callCtrE");
    }
}
