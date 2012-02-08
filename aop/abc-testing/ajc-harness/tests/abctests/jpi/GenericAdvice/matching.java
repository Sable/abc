import java.lang.*;
import org.aspectj.testing.Tester;

<R> jpi R JP();
<R, T>jpi R JP2(T a);

aspect A{
	<R> R around JP(){
		C.counterR++;
		return proceed();
	}
	
	<R, T> R around JP2(T y){
		C.counterRT++;
		System.out.println(thisJoinPoint.getSourceLocation());
		return proceed(y);
	}
}

public class C{
	
	public static int counterR = 0;
	public static int counterRT = 0;
	
	<R, T> exhibits R JP2(T d) : call(* *(..)) && target(d);
	
	<R> exhibits R JP() : call(* *(..));
		
	public Integer foo2(){
		return null;
	}
	
	public Float bar2(){
		return null;
	}
	
	public static void main(String[] args){
		C e = new C();
		e.foo2();
		e.bar2();
		Tester.checkEqual(counterR,2,"expected 2 matches but saw "+counterR);
		Tester.checkEqual(counterRT,5,"expected 5 matches but saw "+counterRT);
	}	
}