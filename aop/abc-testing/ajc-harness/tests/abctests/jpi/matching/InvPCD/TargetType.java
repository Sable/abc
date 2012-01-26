import org.aspectj.testing.Tester;

jpi void JP(A o);
jpi void JP2(A i);


class A{
	
	public void foo(){}
	
}

class B extends A{
	
	public void foo(){}
}

class C extends B{
	
	public void foo(){}
	
}


public aspect AS{
	
	public static int countVariant = 0;
	public static int countInvariant = 0;
	
	exhibits void JP(A a) : call(void *(..)) && target(a);
	
	exhibits void JP2(A a) : call(void *(..)) && targetinv(a);

	public static void main(String[] args){
		A a = new A();
		B b = new B();
		C c = new C();
		
		a.foo();
		b.foo();
		c.foo();
		Tester.checkEqual(AS.countVariant,3,"expected 3 matches but saw "+AS.countVariant);		
		Tester.checkEqual(AS.countInvariant,1,"expected 1 matches but saw "+AS.countInvariant);		
	}
	
	before JP(A z){
		AS.countVariant++;
		System.out.println("JPI: variant");		
	}
	
	before JP2(A l){
		AS.countInvariant++;
		System.out.println("JPI: invariant");
	}	
}