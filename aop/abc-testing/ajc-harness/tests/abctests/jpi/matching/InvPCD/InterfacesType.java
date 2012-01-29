import org.aspectj.testing.Tester;

jpi void JP();
jpi void JP2(I a);

interface I{
	public void bar();
	public void zar();
}

class A implements I{
	
	exhibits void JP() : call(void *(..)) && target(I);

	exhibits void JP2(I a) : call(void *(..)) && targetinv(a);

	public void bar(){
		zar();
	}
	
	public void zar(){}
	
}


public aspect AS{
	
	public static int count = 0;
	
	exhibits void JP() : call(void *(..)) && target(I);

	exhibits void JP2(I a) : call(void *(..)) && targetinv(a);

	
	before JP(){
		AS.count++;
		System.out.println("JPI: capture variant");
	}
	
	before JP2(I a){
		AS.count++;
	}
	
	public static void main(String[] args){
		A a = new A();
		a.bar();
		a.zar();
		Tester.checkEqual(AS.count,6,"expected 6 matches but saw "+AS.count);
		
	}
	
}