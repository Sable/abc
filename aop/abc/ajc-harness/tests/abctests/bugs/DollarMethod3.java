import org.aspectj.testing.Tester;

aspect DollarMethod3 {
    after():call(void Some$Type.someMethod()){
	// should advise all calls to methods named some$Method 
	Tester.event("Advising " + thisJoinPoint);
    }
}

class Some$Type{
    public static void main(String args[]){
	Some$Type m = new Some$Type();
	m.someMethod();
	Tester.expectEvent("Hoping to be advised (void Some$Type.someMethod())");
	Tester.expectEvent("Advising call(void Some$Type.someMethod())");
	Tester.checkAllEvents();
    }
    public void someMethod(){
	Tester.event("Hoping to be advised (void Some$Type.someMethod())");
    }
}