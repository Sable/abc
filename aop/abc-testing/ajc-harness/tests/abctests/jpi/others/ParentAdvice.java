
jpi void A();
jpi void B() extends A();


public class C{
	
	exhibits void B() : call(* foo(..));
	
	public void foo(){}
	
	public static void main(String[] args){
		new C().foo();
	}
}

aspect M{
	
	void around A(){
		
	}
}
