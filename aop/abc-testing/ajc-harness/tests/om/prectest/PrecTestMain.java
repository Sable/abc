import org.aspectj.testing.Tester;

//Main file for Module1
public class PrecTestMain {
    public static void main(String args[]) {
        A a = new A();
        int x = a.fib(10);
        
        System.out.println(x);
        System.out.println(Fib.callCtr);
        System.out.println(ACache.callCtr);
        System.out.println(PrecFibExtAspect.callCtr);
        
        Tester.checkEqual(Fib.callCtr, 10, "Fib.callCtr");
        Tester.checkEqual(ACache.callCtr, 17, "ACache.callCtr");
        Tester.checkEqual(PrecFibExtAspect.callCtr, 1, "PrecFibExtAspect.callCtr");
    }
}