import java.lang.*;

<R> jpi int JP();
<R extends Integer> jpi R JP1(R a);

aspect A{

	<L extends Integer> L around JP1(L b){
		L r = proceed(b);
		System.out.println("result "+r.toString());
		return r;
	}
	
	<L extends Integer> before JP1(L i){
		System.out.println("before "+i.toString());
	}

	<L extends Integer> after JP1(L i) returning(L o){
		System.out.println("after returning, return value :"+o.toString());		
	}		
	
	<L extends Integer> after JP1(L i){
		System.out.println("after "+i.toString());		
	}
	
}


public class C{
	
	<M extends Integer> exhibits M JP1(M k) : call(M *(..)) && Args(k);
	
	public static Integer foo(Integer a){return 1;}
	
	public static void main(String[] args){
		foo(new Integer(3));
	}
	
	
}