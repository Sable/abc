import org.aspectj.testing.Tester;

public class Handler {

    public static void main(String[] args) {
	try {
	    throw new RuntimeException();
	} catch(RuntimeException e) {
	}
       Tester.expectEvent("ran advice");
       Tester.checkAllEvents();
    }

}

aspect HandlerAspect {

    before(): handler(RuntimeException) && cflow(handler(RuntimeException)) {
	Tester.event("ran advice");
    }
}
