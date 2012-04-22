import org.aspectj.testing.Tester;

class A{}
class B extends A{}
class C extends B{}
class D extends C{}

jpi void JP(int i);
jpi void JP2(int a);
jpi void JP3(A a);
jpi void JP4(A a);

public aspect AS{
	
	public static int countPrimitives = 0;
	public static int countReferenced = 0;

	//primitive types	
	exhibits void JP(int a) : call(void *(..)) && Args(a);
	exhibits void JP2(int a) : call(void *(..)) && args(a);

	public static void foo(char x){}
	public static void bar(byte x){}
	public static void zoo(short x){}
	
	//Referenced types
	exhibits void JP3(A a) : call(void *(..)) && Args(a);
	exhibits void JP4(A a) : call(void *(..)) && args(a);
	
	public static void foo2(B x){}
	public static void bar2(C x){}
	public static void zoo2(D x){}
	
	
	public static void main(String[] args){
		char i = 1;
		byte ii = 1;
		short iii = 1;
		//primitive types
		foo(i);
		bar(ii);
		zoo(iii);
		Tester.checkEqual(AS.countPrimitives,3,"expected 3 matches but saw "+AS.countPrimitives);		
		//referenced types
		foo2(new B());
		bar2(new C());
		zoo2(new D());		
		Tester.checkEqual(AS.countReferenced,3,"expected 3 matches but saw "+AS.countReferenced);
	}

	before JP(int c){
		Tester.check(false,"this advice should not get executed");		
	}

	before JP2(int b){
		AS.countPrimitives++;
		System.out.println("JPI: variant");		
	}

	before JP3(A c){
		Tester.check(false,"this advice should not get executed");		
	}

	before JP4(A b){
		AS.countReferenced++;
		System.out.println("JPI: variant");		
	}
}
