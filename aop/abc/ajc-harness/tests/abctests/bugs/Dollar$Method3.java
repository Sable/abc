import org.aspectj.testing.Tester;

aspect TestAspect {
    after():call(void Dollar$Method3.someMethod()){
	// should advise all calls to methods named some$Method 
	Tester.event("Advising " + thisJoinPoint);
    }
}

public class Dollar$Method3 {
    public static void main(String args[]){
	Dollar$Method3 m = new Dollar$Method3();
	m.someMethod();
	Tester.expectEvent("Hoping to be advised (void Dollar$Method3.someMethod())");
        // Note that thisJoinPoint reports this class as Dollar.Method3 ; 
        // it's not clear to me (Ganesh) whether this is correct behaviour 
        // or not, but for now I'm just accepting it
	Tester.expectEvent("Advising call(void Dollar.Method3.someMethod())");
	Tester.checkAllEvents();
    }
    public void someMethod(){
	Tester.event("Hoping to be advised (void Dollar$Method3.someMethod())");
    }
}
