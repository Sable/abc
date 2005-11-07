import org.aspectj.testing.Tester;

class A {
    String x;
    public A(String x) {
        this.x = x;
    }
}
class B extends A {
    public B(String x) {
        super(x);
    }
    public B(String x, int y) {
        this(x + "from B");
    }
}

public class Preinit {
    public static void main(String[] args) {
        B b = new B("b",3);
    }
}

aspect JoinPointTraceAspect {

	pointcut tracePoints() : (call(*.new(..)) || preinitialization(*.new(..))) &&
                                 !within(JoinPointTraceAspect);
	   
	after() : tracePoints() {
	    System.out.println(thisJoinPoint);
	   
	}

      
} 
