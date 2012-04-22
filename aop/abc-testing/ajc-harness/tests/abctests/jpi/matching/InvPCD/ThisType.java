import org.aspectj.testing.Tester;

jpi void JP(A o);
jpi void JP2(A i);

class A{
	
	exhibits void JP(A l) : call(void *(..)) && this(l);
	exhibits void JP2(A k) : call(void *(..)) && This(k);
	
	public void bar(){
		zar();
	}
	
	public void zar(){}
	
}

class B extends A{
	
	exhibits void JP(A l) : call(void *(..)) && this(l);
	exhibits void JP2(A k) : call(void *(..)) && This(k);	
	
	public void foo(){
		bar();
		zar();
	}
}


public aspect AS{

	exhibits void JP(A l) : call(void *(..)) && this(l);
	exhibits void JP2(A k) : call(void *(..)) && This(k);	

	public static int countVariant = 0;
	public static int countInvariant = 0;	
	
	public static void main(String[] args){
		B b = new B();
		b.foo();
		Tester.checkEqual(AS.countVariant,3,"expected 3 matches but saw "+AS.countVariant);
		Tester.checkEqual(AS.countInvariant,1,"expected 1 matches but saw "+AS.countInvariant);
	}
	
	before JP(A m){
		AS.countVariant++;
		System.out.println("JPI: variant");			
	}

	before JP2(A m){
		AS.countInvariant++;
		System.out.println("JPI: invariant");	
	}

	
}