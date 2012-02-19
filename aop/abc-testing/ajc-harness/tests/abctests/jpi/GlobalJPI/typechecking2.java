import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Integer> global jpi R JP() : call(* myfoo(..));
jpi Float JP1();

class A{
	<M extends Integer> exhibits M JP() : execution(* bar(..));
	exhibits Float JP1() : execution(Float bar(..));
	
	public static Integer foo(){return null;}
	public static Float bar(){return null;} //error
}

public class C{
	
	public static String myfoo(){return null;}		
	public static void main(String[] args){
		A.foo();
		myfoo(); //error
	}
}
