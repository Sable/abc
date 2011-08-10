/*
 * This test case is used to see the correct replacement of the bindings
 * in the args, this, target pointcuts designator.  Those replacements
 * are verifying in debugging time.
 */

import java.lang.*;
import org.aspectj.testing.Tester;

jpi Integer H();
jpi Integer JP(int i, String z) extends H();
jpi Integer Z(int b, String j);

public class CheckPointcutExpressionsArgs{
	exhibits Integer H() : call(* foo(..));
	exhibits Integer JP(int z, String b) : call(* foo(..)) && args(z,b);	
	exhibits Integer Z(int l, String g) : call(* foo(..)) && args(l,g);
	
	Integer foo(int x, Integer z){return null;}
	
	public static void main(String[] args){
		new CheckPointcutExpressionsArgs().foo(5,3);		
	}
}


aspect A{

	exhibits Integer Z(int f, String r) : call(* foo(..)) && args(f,r);
	
	Integer around Z(int m, String I){
		Tester.check(false,"this advice should not execute");
		return null;
	}

	Integer around H(){
		Tester.check(true,"this advice should execute!");
		return null;		
	}
	
	Integer around JP(int i, String l){
		Tester.check(false,"this advice should not execute");
		return null;		
	}	
}