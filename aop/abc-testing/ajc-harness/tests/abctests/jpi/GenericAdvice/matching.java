
import java.lang.*;
import org.aspectj.testing.Tester;

<R> jpi R JP();
<R extends Integer>jpi R JP(R a);
<R extends Number> jpi R JP2();

aspect A{
	<R> R around JP(){
		C.counterJP++;
		return proceed();
	}
	
	<I extends Integer> I around JP(I y){
		C.counterJPI++;
		return proceed(y);
	}

	<I extends Number> I around JP2(){
		C.counterJP2++;
		return proceed();
	}
}

public class C{
	
	public static int counterJP = 0;
	public static int counterJPI = 0;
	public static int counterJP2 = 0;

	<R> exhibits R JP() : execution(* *(..));
	<R extends Integer> exhibits R JP(R d) : execution(R *(..)) && Args(d);
	<R extends Number> exhibits R JP2() : execution(R *(..));
		
	public static Integer foo(Integer a){return null;}
	public static Float bar(Integer b){return null;}
	public static Integer foo2(Integer l){return null;}
	public static Float bar2(Integer a){return null;}
	
	public static void main(String[] args){
		foo(new Integer(1));
		bar(new Integer(2));
		foo2(new Integer(3));
		bar2(new Integer(4));
		Tester.checkEqual(counterJP,5,"expected 5 matches but saw "+counterJP); //4 + 1(match on main(String[]) execution)
		Tester.checkEqual(counterJPI,2,"expected 2 matches but saw "+counterJPI);
		Tester.checkEqual(counterJP2,4,"expected 4 matches but saw "+counterJP2);		
	}	
}


//aspect APS{
//	before() : execution(Integer+ *(..)){
//		System.out.println("hola");
//	}
//}
//
//public class C{
//	
//	public static int counterJP = 0;
//	public static int counterJPI = 0;
//	public static int counterJP2 = 0;
//
//	public static Integer ofoo(Integer a){return null;}
//	public static Float obar(Integer b){return null;}
//	public static Integer ofoo2(Integer l){return null;}
//	public static Float obar2(Integer a){return null;}
//	
//	public static void main(String[] args){
//		ofoo(new Integer(1));
//		obar(new Integer(2));
//		ofoo2(new Integer(3));
//		obar2(new Integer(4));
//	}	
//}