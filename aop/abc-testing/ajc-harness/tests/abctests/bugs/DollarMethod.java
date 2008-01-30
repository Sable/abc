import org.aspectj.testing.Tester;

aspect TestAspect {
    after(): call(void *.some$Method()){
	// should advise all calls to methods named some$Method 
	Tester.event("Advising " + thisJoinPoint);
    }
}

public class DollarMethod {
    public static void main(String args[]){
	DollarMethod m = new DollarMethod();
	m.some$Method();
	Tester.expectEvent("Hoping to be advised (void DollarMethod.some$Method())");
	Tester.expectEvent("Advising call(void DollarMethod.some$Method())");
	Tester.checkAllEvents();
    }
    public void some$Method(){
	Tester.event("Hoping to be advised (void DollarMethod.some$Method())");
    }
}