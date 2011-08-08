import java.util.*;
import java.lang.*;

jpi boolean JP6(String l, int s);
//jpi int JP();
//jpi int JP3();

public class C{
	
	exhibits boolean JP6(String j, int k): call(* *.foo(..)) && args(j,k);
	
	public static Collection foo(String j, int m){return null;}
	
	public static void bar(){
		java.util.Collection b = foo("",4);
		//System.out.println(b);
	}
	
	public static void main(String[] args){
		bar();
	}
	
}

//class Milton{exhibits java.util.Collection JP6(String www, int p) : call(* *.foo(..)) && args(www,p);}
/*
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
*/
/*
aspect M{
	
	int around JP(int k){
		return proceed(k);
	}
	
	Object around() : call(* *.*(..)){
		return new Object();
	}	
}
*/
//aspect J{java.util.Collection around(int i, String l) : call(* foo(..)) && args(i, l){return null;}}
