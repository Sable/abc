import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Integer> jpi R JP(R x);

aspect A{
	<I extends Integer> I around JP(I i){
		I a = proceed(i);
		System.out.println(a.doubleValue());
		return a;
	}
}

public class C{
	
	<R extends Integer> exhibits R  JP(R r) : call(Integer *(..)) && argsinv(r);
		
	public Integer foo2(Integer u){return u;}
	
	public Integer bar2(Integer l){return l;}
	
	public static void main(String[] args){
		C e = new C();
		e.foo2(4);
		e.bar2(4);
	}	
}

//public class C{
//	
//	public static <R> R foo(R h){
//		R a = null;
//		return null;
//	}
//	
//	public static <L> void main(String[] arsg){
//		L o =foo(null);
//		System.out.println(o);
//	}
//	
//}