import java.io.IOException;
//
//<R extends IOException> jpi int JP() throws IOException;
//
//public class C{
//	<L extends IOException> exhibits int JP() : call(int *(..) throws L);
//	
//	public static int a() throws IOException{return 1;}
//	
//	public static int b(){return 1;}
//	
//	public static void main(String[] args){
//		try{
//			a();
//		}
//		catch(Exception e){}
//		b();
//	}
//}
//
//aspect A{
//	
//	int around JP(){
//		try{
//			return proceed();
//		}
//		catch(IOException e){}
//		catch(IOException e){}		
//		finally{
//			return 1;
//		}
//	}
//}

//class Point{}
//class FixedPoint extends Point{}
//class ColorFixedPoint extends FixedPoint{}
//
//jpi Point JP();
//
//public class C{
//	
//	exhibits Point JP() : call(Point+ *(..));
//	
//	public static Point foo(){return null;}
//	public static FixedPoint bar(){return null;}
//	public static ColorFixedPoint zar(){return null;}
//	
//	public static void main(String[] args){
//		foo();
//		bar();
//		zar();
//	}
//}
//
//aspect AS{
//	
//	Point around JP(){
//		System.out.println("pasé");
//		return proceed();
//	}
//}

<E extends Exception> global jpi void JP() throws Exception : execution(void foo(..));

class C{
	
	void foo() throws Exception, IOException {}
	
	public static void main(String[] args){
		C a  = new C();
		a.foo();
	}
}

aspect A{
	void around JP(){
		try{
			proceed();
		}
		catch(Exception e){
			
		}
	}
}