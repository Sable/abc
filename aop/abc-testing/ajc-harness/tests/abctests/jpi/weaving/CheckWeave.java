/*
 * This is not really a test case, because I used it to check the weave semantics when I was developing
 * that feature;
 */


import java.util.*;
import java.lang.*;

jpi boolean JP6(String l, int s);

public class C{
	
	exhibits boolean JP6(String j, int k): //must be boolean not Collection 
		call(* *.foo(..)) && args(j,k);
	
	public static Collection foo(String j, int m){return null;}
	
	public static void bar(){
		java.util.Collection b = foo("",4); //error marked here :-(
	}
	
	public static void main(String[] args){
		bar();
	}	
}