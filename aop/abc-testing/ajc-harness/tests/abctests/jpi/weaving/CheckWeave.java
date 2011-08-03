jpi int JP(int i);

public class C{
	
	exhibits int JP(int h): call(* *.foo(..)) && args(h);
	
	public void foo(int t){
		new A().foo(6);
	}
	
	public static void main(String[] args){
		new C().foo(6);
	}
	
}

class A{
	public void foo(int l){}
}

aspect Z{
	
	/*int around JP(int k){
		return proceed(k);
	}*/
}