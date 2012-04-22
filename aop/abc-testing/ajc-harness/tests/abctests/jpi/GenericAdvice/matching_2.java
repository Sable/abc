import java.lang.*;
import org.aspectj.testing.Tester;

<R extends Number>jpi R JP();
<R extends Number>jpi R JP(R a);
<R extends Number>jpi R JP1(R a);
<R extends Number>jpi R JP2(R a);

aspect A{
	<I extends Number> I around JP(I y){
		C.counterJPI++;
		return proceed(y);
	}
}

public class C{
	
	public static int counterJPI = 0;
	
	<R extends Number> exhibits R JP() : execution(R *(..)) && Args(R) && This(R) && Target(R);
	<R extends Number> exhibits R JP(R d) : execution(R *(..)) && Args(d);
	<R extends Number> exhibits R JP1(R d) : execution(R *(..)) && This(d);
	<R extends Number> exhibits R JP2(R d) : execution(R *(..)) && Target(d);
}
