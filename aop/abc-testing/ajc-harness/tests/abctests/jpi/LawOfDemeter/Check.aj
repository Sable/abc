import java.lang.*;

aspect A{
	
	before() : call(* *(Number)){
		System.out.println("Hola");
	}
	
}

public class Check{
	
	public static void foo(Integer l){}
	
	public static void main(String[] args){
		foo(5);
	}
	
}
