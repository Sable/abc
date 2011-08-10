import org.aspectj.testing.Tester;

jpi void H();
jpi void JP(int i, String z) extends H();
jpi void Z(int b, String j);
/*
jpi void JP1() extends JP();
jpi void JP2() extends JP();

jpi void AJP();

class Z{
	exhibits void JP() : call(* foo());
	exhibits void H() : call(* *.*(..));
}

class W{
	exhibits void JP1() : call(* foo(..)) && within(W);
}
*/
public class CheckPointcutExpressionsArgs{
	exhibits void H() : call(* foo(..));
	exhibits void JP(int z, String b) : call(* foo(..)) && args(z,b);
	
	exhibits void Z(int l, String g) : call(* foo(..)) && args(l,g);
	
	void foo(int x, Integer z){}
	
	public static void main(String[] args){
		new CheckPointcutExpressionsArgs().foo(5,3);		
	}
}


aspect A{

	exhibits void Z(int f, String r) : call(* foo(..)) && args(f,r);
	

	void around Z(int m, String I){
		
	}

	void around H(){
	}
	
	void around JP(int i, String l){
		Tester.check(false,"this advice should not execute");				
	}
	
}	
/*	void around JP1(){}

	void around JP2(){}
	
	void around AJP(){}
}

aspect B{
	void around H(){}	
	
	void around JP(){}
	
	void around JP1(){}
}

aspect C{

	void around H(){}
	
	void around JP(){}
	
	void around JP2(){}
}

aspect D{
	void around JP(){}

	void around H(){}
}


aspect z{
	void around() : call(* foo()) && within(z){}
}

*/