import org.aspectj.testing.Tester;

//Main file for Module2
public class PrecTestMain2 {
    public static void main(String args[]) {
        A a = new A();
        int x = a.fib(10);
        
        System.out.println(x);
        System.out.println(Fib.callCtr);
        System.out.println(ACache.callCtr);
        System.out.println(PrecFibExtAspect.callCtr);
        
        Tester.checkEqual(Fib.callCtr, 109, "Fib.callCtr");
        Tester.checkEqual(ACache.callCtr, 55, "ACache.callCtr");
        Tester.checkEqual(PrecFibExtAspect.callCtr, 1, "PrecFibExtAspect.callCtr");
    }
}