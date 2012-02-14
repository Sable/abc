import java.lang.*;

<R> jpi int JP();
<R extends Integer> jpi R JP1();
<R> jpi R JP2(R a);

aspect A{
	
	<L extends Integer> L around JP1(){
		L a = new Object(); //error		
		return ""; //error
	}

	<L extends Integer> L around JP1(){
		L a = proceed();
		a = new Integer(3); //error
		return a;
	}	

	<L extends Integer> L around JP1(){
		return new Integer(5); //error
	}	

	<L extends Integer> L around JP1(){
		B a = proceed(); //error
		return a; 
	}	
	
	<L extends Integer> L around JP1(){
		Integer a = (Integer)proceed(); //error
		return a; //error 
	}	
		
	<L> L around JP2(L a){
		a = null; //ok
		a = new Object(); //error
		proceed(null); //ok;
		L b = proceed(a); //ok
		L z = null; //ok
		return null; //ok
		//return proceed(null); //ok
	}	
}

/*
class A{
	
	public static <R extends Integer> R foo(R a){
		R b = new Object();
		return "";
	}
	
}
*/