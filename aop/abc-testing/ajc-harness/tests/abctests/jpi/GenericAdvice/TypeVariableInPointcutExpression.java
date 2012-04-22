import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Integer> jpi R JP();

<J extends Float> jpi J JP(J l);

public class C{
	
	public static int IntegerCounter = 0;
	public static int FloatCounter = 0;
	
	
	<J extends Integer> exhibits J JP() : execution(J *(..));

	<J extends Float> exhibits J JP(J m) : execution(J *(..)) && Args(m);
	
	public static Integer foo(){return null;}
	public static Integer bar(){return null;}
	
	public static Float foo2(Float r){return null;}
	public static Float bar2(Float r){return null;}
	public static Float zar(Float r){return null;}
	
	
	public static void main(String[] args){
		foo();
		bar();
		foo2(new Float(5));
		bar2(new Float(5));
		zar(new Float(5));		
		Tester.checkEqual(IntegerCounter,2,"expected 2 matches but saw "+IntegerCounter);
		Tester.checkEqual(FloatCounter,3,"expected 3 matches but saw "+FloatCounter);		
	}
}

aspect L{

	<R extends Integer> R around JP(){
		C.IntegerCounter++;
		return proceed();
	}

	<R extends Float> R around JP(R o){
		C.FloatCounter++;
		return proceed(o);
	}
}