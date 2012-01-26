import org.aspectj.testing.Tester;

jpi void JP();
jpi void JP2();

interface I{
	public void bar();
	public void zar();
}

class A implements I{
	
	exhibits void JP() : call(void *(..)) && target(I);

	exhibits void JP2() : call(void *(..)) && targetinv(I);

	public void bar(){
		zar();
	}
	
	public void zar(){}
	
}


public aspect AS{
	
	public static int count = 0;
	
	exhibits void JP() : call(void *(..)) && target(I);

	exhibits void JP2() : call(void *(..)) && targetinv(I);

	
	before JP(){
		count++;
		System.out.println("JPI: capture variant");
	}
	
	before JP2(){
		Tester.check(false,"this advice should not get executed");
	}
	
	public static void main(String[] args){
		A a = new A();
		a.bar();
		a.zar();
		Tester.checkEqual(AS.count,3,"expected 3 matches but saw "+AS.count);
		
	}
	
}