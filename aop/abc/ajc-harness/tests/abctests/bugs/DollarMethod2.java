import org.aspectj.testing.Tester;

aspect TestAspect {
    after():call(void *.some$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$()){
	// should advise all calls to methods named some$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	Tester.event("Advising " + thisJoinPoint);
    }
}

public class DollarMethod2 {
    public static void main(String args[]){
	DollarMethod2 m = new DollarMethod2();
	m.some();
	Tester.expectEvent("Hoping NOT to be advised (void DollarMethod2.some())");
	Tester.checkAllEvents();
    }
    public void some(){
	Tester.event("Hoping NOT to be advised (void DollarMethod2.some())");
    }
}